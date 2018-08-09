package core.framework.test.redis;

import core.framework.redis.RedisList;
import core.framework.util.Exceptions;

import java.util.Collections;
import java.util.List;

/**
 * @author rexthk
 */
public final class MockRedisList implements RedisList {
    private final MockRedis redis;

    MockRedisList(MockRedis redis) {
        this.redis = redis;
    }

    @Override
    public String pop(String key) {
        MockRedis.Value value = redis.store.get(key);
        if (value == null) return null;
        validate(key, value);
        return value.list.remove(0);
    }

    @Override
    public void push(String key, String... values) {
        MockRedis.Value value = redis.store.computeIfAbsent(key, k -> MockRedis.Value.listValue());
        validate(key, value);
        Collections.addAll(value.list, values);
    }

    @Override
    public List<String> range(String key, int start, int end) {
        MockRedis.Value value = redis.store.get(key);
        if (value == null) return List.of();
        validate(key, value);
        return List.copyOf(value.list);
    }

    private void validate(String key, MockRedis.Value value) {
        if (value.type != MockRedis.ValueType.LIST) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
    }
}
