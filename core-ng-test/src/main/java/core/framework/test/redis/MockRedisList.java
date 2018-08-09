package core.framework.test.redis;

import core.framework.redis.RedisList;
import core.framework.util.Exceptions;
import core.framework.util.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rexthk
 */
public final class MockRedisList implements RedisList {
    private final MockRedis redis;

    public MockRedisList(MockRedis redis) {
        this.redis = redis;
    }


    @Override
    public String pop(String key) {
        MockRedis.Value popValue = redis.store.get(key);
        if (popValue == null) return null;
        validate(key, popValue);
        String value = popValue.list.get(0);
        popValue.list.remove(0);
        return value;
    }

    @Override
    public void push(String key, String value) {
        MockRedis.Value listValue = redis.store.computeIfAbsent(key, k -> MockRedis.Value.listValue());
        validate(key, listValue);
        listValue.list.add(value);
    }

    @Override
    public List<String> getAll(String key) {
        MockRedis.Value value = redis.store.get(key);
        if (value == null) return Lists.newArrayList();
        validate(key, value);
        return new ArrayList<>(value.list);
    }

    private void validate(String key, MockRedis.Value value) {
        if (value.type != MockRedis.ValueType.LIST) throw Exceptions.error("invalid type, key={}, type={}", key, value.type);
    }
}
