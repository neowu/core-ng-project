package core.framework.test.redis;

import core.framework.redis.RedisSet;
import core.framework.util.Exceptions;
import core.framework.util.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public final class MockRedisSet implements RedisSet {
    private final MockRedis redis;

    public MockRedisSet(MockRedis redis) {
        this.redis = redis;
    }

    @Override
    public boolean add(String key, String value) {
        MockRedis.Value setValue = redis.store.computeIfAbsent(key, k -> MockRedis.Value.setValue());
        validate(key, setValue);
        return setValue.set.add(value);
    }

    @Override
    public Set<String> members(String key) {
        MockRedis.Value value = redis.store.get(key);
        if (value == null) return Sets.newHashSet();
        validate(key, value);
        return new HashSet<>(value.set);
    }

    @Override
    public boolean isMember(String key, String value) {
        MockRedis.Value redisValue = redis.store.get(key);
        if (redisValue == null) return false;
        validate(key, redisValue);
        return redisValue.set.contains(value);
    }

    @Override
    public boolean remove(String key, String... values) {
        MockRedis.Value redisValue = redis.store.get(key);
        if (redisValue == null) return false;
        validate(key, redisValue);
        return redisValue.set.removeAll(Arrays.asList(values));
    }

    private void validate(String key, MockRedis.Value value) {
        if (value.type != MockRedis.ValueType.SET) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
    }
}
