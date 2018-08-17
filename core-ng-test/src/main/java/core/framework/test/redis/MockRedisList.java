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
    public long push(String key, String... values) {
        assertThat(values).doesNotContainNull();
        var value = store.putIfAbsent(key, new ArrayList<>(values.length));
        List<String> list = value.list();
        Collections.addAll(list, values);
        return list.size();
    }

    @Override
    public List<String> range(String key, long start, long end) {
        var value = store.get(key);
        if (value == null) return List.of();
        List<String> list = value.list();
        int size = list.size();
        int startIndex = start < 0 ? 0 : (int) start;
        if (startIndex > size) startIndex = size;
        int endIndex = end < 0 ? (int) end + size : (int) end;
        if (endIndex >= size) endIndex = size - 1;
        return List.copyOf(list.subList(startIndex, endIndex + 1));
    }
}
