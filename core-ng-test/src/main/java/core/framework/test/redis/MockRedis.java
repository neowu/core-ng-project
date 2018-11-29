package core.framework.test.redis;

import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisList;
import core.framework.redis.RedisSet;
import core.framework.util.Maps;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author neo
 */
public final class MockRedis implements Redis {
    private final MockRedisStore store = new MockRedisStore();
    private final MockRedisHash hash = new MockRedisHash(store);
    private final MockRedisSet set = new MockRedisSet(store);
    private final MockRedisList list = new MockRedisList(store);

    @Override
    public String get(String key) {
        var value = store.get(key);
        if (value == null) return null;
        return value.string();
    }

    @Override
    public boolean set(String key, String value, Duration expiration, boolean onlyIfAbsent) {
        MockRedisStore.Value redisValue = new MockRedisStore.Value(value);
        if (expiration != null) redisValue.expirationTime = System.currentTimeMillis() + expiration.toMillis();
        if (onlyIfAbsent) {
            return store.store.putIfAbsent(key, redisValue) == null;
        } else {
            store.store.put(key, redisValue);
            return true;
        }
    }

    @Override
    public RedisSet set() {
        return set;
    }

    @Override
    public void expire(String key, Duration duration) {
        var value = store.get(key);
        if (value != null) {
            value.expirationTime = System.currentTimeMillis() + duration.toMillis();
        }
    }

    @Override
    public long del(String... keys) {
        long removed = 0;
        for (String key : keys) {
            if (store.store.remove(key) != null) removed++;
        }
        return removed;
    }

    @Override
    public long increaseBy(String key, long increment) {
        var value = store.get(key);
        if (value == null) {
            value = new MockRedisStore.Value("0");   // according to https://redis.io/commands/incrby, set to 0 if key not exists
        }
        long result = Long.parseLong(value.string()) + increment;
        store.store.put(key, new MockRedisStore.Value(String.valueOf(result)));
        return result;
    }

    @Override
    public Map<String, String> multiGet(String... keys) {
        Map<String, String> results = Maps.newLinkedHashMapWithExpectedSize(keys.length);
        for (String key : keys) {
            String value = get(key);
            if (value != null) results.put(key, value);
        }
        return results;
    }

    @Override
    public void multiSet(Map<String, String> values) {
        values.forEach(this::set);
    }

    @Override
    public RedisHash hash() {
        return hash;
    }

    @Override
    public void forEach(String pattern, Consumer<String> consumer) {
        KeyMatcher matcher = new KeyMatcher(pattern);
        for (String key : store.store.keySet()) {
            if (matcher.matches(key)) {
                consumer.accept(key);
            }
        }
    }

    @Override
    public RedisList list() {
        return list;
    }
}
