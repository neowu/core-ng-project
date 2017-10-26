package core.framework.test.redis;

import core.framework.redis.RedisHash;
import core.framework.util.Exceptions;
import core.framework.util.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public final class MockRedisHash implements RedisHash {
    private final MockRedis redis;

    public MockRedisHash(MockRedis redis) {
        this.redis = redis;
    }

    @Override
    public Map<String, String> getAll(String key) {
        MockRedis.Value value = redis.store.get(key);
        if (value == null) return Maps.newHashMap();
        validate(key, value);
        return new HashMap<>(value.hash);
    }

    @Override
    public String get(String key, String field) {
        MockRedis.Value value = redis.store.get(key);
        if (value == null) return null;
        validate(key, value);
        return value.hash.get(field);
    }

    @Override
    public void set(String key, String field, String value) {
        MockRedis.Value hashValue = redis.store.computeIfAbsent(key, k -> MockRedis.Value.hashValue());
        validate(key, hashValue);
        hashValue.hash.put(field, value);
    }

    @Override
    public void multiSet(String key, Map<String, String> values) {
        MockRedis.Value hashValue = redis.store.computeIfAbsent(key, k -> MockRedis.Value.hashValue());
        validate(key, hashValue);
        hashValue.hash.putAll(values);
    }

    @Override
    public boolean del(String key, String... fields) {
        MockRedis.Value hashValue = redis.store.computeIfAbsent(key, k -> MockRedis.Value.hashValue());
        validate(key, hashValue);
        boolean deleted = false;
        for (String field : fields) {
            String previous = hashValue.hash.remove(field);
            if (previous != null) deleted = true;
        }
        return deleted;
    }

    private void validate(String key, MockRedis.Value value) {
        if (value.type != MockRedis.ValueType.HASH) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
    }
}
