package core.framework.test.redis;

import core.framework.redis.RedisSortedSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author tempo
 */
public class MockRedisSortedSet implements RedisSortedSet {
    private final MockRedisStore store;

    MockRedisSortedSet(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public int add(String key, Map<String, Long> values, boolean onlyIfAbsent) {
        var sortedSet = store.putIfAbsent(key, new MockRedisStore.SortedSet()).sortedSet();
        if (onlyIfAbsent) {
            int added = 0;
            for (Entry<String, Long> entry : values.entrySet()) {
                if (sortedSet.putIfAbsent(entry.getKey(), entry.getValue()) == null) {
                    added++;
                }
            }
            return added;
        } else {
            sortedSet.putAll(values);
            return values.size();
        }
    }

    @Override
    public Map<String, Long> range(String key, long start, long stop) {
        var value = store.get(key);
        if (value == null) return Map.of();
        var sortedSet = value.sortedSet();
        int size = sortedSet.size();
        int startIndex = start < 0 ? 0 : (int) start;
        if (startIndex > size) startIndex = size;
        int endIndex = stop < 0 ? (int) stop + size : (int) stop;
        if (endIndex >= size) endIndex = size - 1;
        return sortedSet.entrySet().stream()
                .sorted(Entry.comparingByValue())
                .skip(startIndex)
                .limit(endIndex - startIndex + 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key2, LinkedHashMap::new));
    }

    @Override
    public Map<String, Long> rangeByScore(String key, long minScore, long maxScore, long limit) {
        var value = store.get(key);
        if (value == null) return Map.of();
        var sortedSet = value.sortedSet();
        return sortedSet.entrySet().stream()
                .filter(entry -> entry.getValue() >= minScore && entry.getValue() <= maxScore)
                .sorted(Entry.comparingByValue())
                .limit(limit == -1 ? Long.MAX_VALUE : limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key2, LinkedHashMap::new));
    }

    @Override
    public Map<String, Long> popByScore(String key, long minScore, long maxScore, long limit) {
        var value = store.get(key);
        if (value == null) return Map.of();
        var sortedSet = value.sortedSet();
        return sortedSet.entrySet().stream()
                .filter(entry -> entry.getValue() >= minScore && entry.getValue() <= maxScore)
                .sorted(Entry.comparingByValue())
                .limit(limit == -1 ? Long.MAX_VALUE : limit)
                .peek(entry -> sortedSet.remove(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key2, LinkedHashMap::new));
    }

    @Override
    public Map<String, Long> popMin(String key, long limit) {
        var value = store.get(key);
        if (value == null) return Map.of();
        var sortedSet = value.sortedSet();
        return sortedSet.entrySet().stream()
            .sorted(Entry.comparingByValue())
            .limit(limit < 1  ? 1 : limit)
            .peek(entry -> sortedSet.remove(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key2, LinkedHashMap::new));
    }

    @Override
    public long removeRangeByScore(String key, long minScore, long maxScore) {
        var value = store.get(key);
        if (value == null) return 0;
        var sortedSet = value.sortedSet();
        var concurrentMapOfSortedSet = new ConcurrentHashMap<>(value.sortedSet());
        return concurrentMapOfSortedSet.entrySet().stream()
            .filter(entry -> entry.getValue() >= minScore && entry.getValue() <= maxScore)
            .peek(entry -> sortedSet.remove(entry.getKey()))
            .count();
    }
}
