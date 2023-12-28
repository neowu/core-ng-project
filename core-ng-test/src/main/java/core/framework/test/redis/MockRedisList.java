package core.framework.test.redis;

import core.framework.redis.RedisList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    public List<String> pop(String key, int size) {
        var value = store.get(key);
        if (value == null) return List.of();
        List<String> list = value.list();

        List<String> results = new ArrayList<>(size);
        long removed = 0;
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (removed == size) break;
            String item = iterator.next();
            iterator.remove();
            results.add(item);
            removed++;
        }
        return results;
    }

    @Override
    public long push(String key, String... values) {
        assertThat(values).isNotEmpty().doesNotContainNull();
        var value = store.putIfAbsent(key, new ArrayList<>(values.length));
        List<String> list = value.list();
        Collections.addAll(list, values);
        return list.size();
    }

    @Override
    public List<String> range(String key, long start, long stop) {
        var value = store.get(key);
        if (value == null) return List.of();
        List<String> list = value.list();
        int size = list.size();
        int startIndex = start < 0 ? (int) start + size : (int) start;
        if (startIndex < 0) startIndex = 0;
        else if (startIndex > size) startIndex = size;
        int endIndex = stop < 0 ? (int) stop + size : (int) stop;
        if (endIndex < 0) endIndex = -1;
        else if (endIndex >= size) endIndex = size - 1;
        return List.copyOf(list.subList(startIndex, endIndex + 1));
    }

    @Override
    public void trim(String key, int maxSize) {
        var value = store.get(key);
        if (value == null) return;

        List<String> list = value.list();
        int size = list.size();
        if (size <= maxSize) return;

        list.subList(0, size - maxSize).clear();
    }
}
