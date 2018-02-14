package core.framework.test.redis;

import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisSet;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Sets;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author neo
 */
public final class MockRedis implements Redis {
    final Map<String, Value> store = Maps.newConcurrentHashMap();

    private final MockRedisHash redisHash = new MockRedisHash(this);
    private final MockRedisSet redisSet = new MockRedisSet(this);

    @Override
    public String get(String key) {
        Value value = value(key);
        if (value == null) return null;
        return value.value;
    }

    private Value value(String key) {
        Value value = store.get(key);
        if (value == null) return null;
        if (value.type != ValueType.VALUE) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
        if (value.expired(System.currentTimeMillis())) {
            store.remove(key);
            return null;
        }
        return value;
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
    public boolean del(String key) {
        Value removed = store.remove(key);
        return removed != null;
    }

    @Override
    public long increaseBy(String key, long increment) {
        Value value = value(key);
        if (value == null) {
            value = Value.value("0");   // according to https://redis.io/commands/incrby, set to 0 if key not exists
            store.put(key, value);
        }
        long result = Long.parseLong(value.value) + increment;
        value.value = String.valueOf(result);
        return result;
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
        KeyMatcher matcher = new KeyMatcher(pattern);
        for (String key : store.keySet()) {
            if (matcher.matches(key)) {
                consumer.accept(key);
            }
        }
    }

    enum ValueType {
        VALUE, HASH, SET
    }

    static final class Value {
        static Value value(String value) {
            return new Value(ValueType.VALUE, value, null, null);
        }

        static Value hashValue() {
            return new Value(ValueType.HASH, null, Maps.newHashMap(), null);
        }

        static Value setValue() {
            return new Value(ValueType.SET, null, null, Sets.newHashSet());
        }

        final ValueType type;
        final Map<String, String> hash;
        final Set<String> set;
        String value;
        Long expirationTime;

        private Value(ValueType type, String value, Map<String, String> hash, Set<String> set) {
            this.type = type;
            this.value = value;
            this.hash = hash;
            this.set = set;
        }

        boolean expired(long now) {
            return expirationTime != null && now >= expirationTime;
        }
    }
}
