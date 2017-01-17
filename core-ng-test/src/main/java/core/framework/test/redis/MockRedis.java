package core.framework.test.redis;

import core.framework.api.redis.Redis;
import core.framework.api.redis.RedisHash;
import core.framework.api.redis.RedisSet;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public final class MockRedis implements Redis {
    final Map<String, Value> store = Maps.newConcurrentHashMap();

    private final MockRedisHash redisHash = new MockRedisHash(this);
    private final MockRedisSet redisSet = new MockRedisSet(this);

    @Override
    public String get(String key) {
        Value value = store.get(key);
        if (value == null) return null;
        if (value.type != ValueType.VALUE) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
        if (value.expired(System.currentTimeMillis())) {
            store.remove(key);
            return null;
        }
        return value.value;
    }

    @Override
    public void set(String key, String value) {
        store.put(key, Value.value(value));
    }

    @Override
    public void set(String key, String value, Duration expiration) {
        Value redisValue = Value.value(value);
        redisValue.expirationTime = System.currentTimeMillis() + expiration.toMillis();
        store.put(key, redisValue);
    }

    @Override
    public RedisSet set() {
        return redisSet;
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration expiration) {
        Value redisValue = Value.value(value);
        redisValue.expirationTime = System.currentTimeMillis() + expiration.toMillis();
        Object previous = store.putIfAbsent(key, redisValue);
        return previous == null;
    }

    @Override
    public void expire(String key, Duration duration) {
        Value value = store.get(key);
        if (value != null) {
            value.expirationTime = System.currentTimeMillis() + duration.toMillis();
        }
    }

    @Override
    public void del(String key) {
        store.remove(key);
    }

    @Override
    public Map<String, String> multiGet(String... keys) {
        Map<String, String> results = Maps.newHashMapWithExpectedSize(keys.length);
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
        return redisHash;
    }

    @Override
    public void forEach(String pattern, Consumer<String> consumer) {
        Pattern keyPattern = Pattern.compile(pattern.replaceAll("\\*", "\\.\\*"));
        for (String key : store.keySet()) {
            if (keyPattern.matcher(key).matches()) {
                consumer.accept(key);
            }
        }
    }

    enum ValueType {
        VALUE, HASH, SET
    }

    static class Value {
        static Value value(String value) {
            Value result = new Value();
            result.type = ValueType.VALUE;
            result.value = value;
            return result;
        }

        static Value hashValue() {
            Value result = new Value();
            result.type = ValueType.HASH;
            result.hash = Maps.newHashMap();
            return result;
        }

        static Value setValue() {
            Value result = new Value();
            result.type = ValueType.SET;
            result.set = Sets.newHashSet();
            return result;
        }

        ValueType type;
        String value;
        Map<String, String> hash;
        Set<String> set;
        Long expirationTime;

        boolean expired(long now) {
            return expirationTime != null && now >= expirationTime;
        }
    }
}
