package core.framework.test.redis;

import core.framework.redis.RedisSortedSet;
import core.framework.util.Maps;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author tempo
 */
public class MockRedisSortedSet implements RedisSortedSet {
    private final MockRedisStore store;

    public MockRedisSortedSet(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public boolean zadd(String key, String value, long score, boolean onlyIfAbsent) {
        var map = store.putIfAbsent(key, new TreeMap<>()).sortedSet();
        if (!onlyIfAbsent) {
            map.remove(value);
        }
        return map.putIfAbsent(value, score) == null;
    }

    @Override
    public Map<String, Long> zrange(String key, long start, long end) {
        var value = store.get(key);
        if (value == null) return Map.of();
        SortedMap<String, Long> map = value.sortedSet();
        int size = map.size();
        int startIndex = start < 0 ? 0 : (int) start;
        if (startIndex > size) startIndex = size;
        int endIndex = end < 0 ? (int) end + size : (int) end;
        if (endIndex >= size) endIndex = size - 1;
        return map.entrySet().stream().sorted(Entry.comparingByValue()).skip(startIndex).limit(endIndex - startIndex + 1).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public Map<String, Long> zpopByScore(String key, long minScore, long maxScore, long limit) {
        MockRedisStore.Value value = store.get(key);
        if (value == null) return Maps.newHashMap();
        var map = value.sortedSet();
        return map.entrySet().stream()
                  .filter(entry -> entry.getValue() <= maxScore)
                  .sorted(Entry.comparingByValue())
                  .limit(limit)
                  .peek(entry -> map.remove(entry.getKey()))
                  .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
