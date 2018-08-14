package core.framework.test.redis;

import core.framework.redis.RedisList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author rexthk
 */
public final class MockRedisList implements RedisList {
    private final MockRedisStore store;

    MockRedisList(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public String pop(String key) {
        var value = store.get(key);
        if (value == null) return null;
        return value.list().remove(0);
    }

    @Override
    public void push(String key, String... values) {
        assertThat(values).doesNotContainNull();
        var value = store.putIfAbsent(key, new ArrayList<>());
        Collections.addAll(value.list(), values);
    }

    @Override
    public List<String> range(String key, int start, int end) {
        var value = store.get(key);
        if (value == null) return List.of();
        return List.copyOf(value.list());
    }
}
