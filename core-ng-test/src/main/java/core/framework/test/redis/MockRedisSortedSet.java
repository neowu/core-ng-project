package core.framework.test.redis;

import core.framework.redis.RedisSortedSet;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author tempo
 */
public class MockRedisSortedSet implements RedisSortedSet {
    private final MockRedisStore store;

    public MockRedisSortedSet(MockRedisStore store) {
        this.store = store;
    }

    @Override
    public boolean push(String key, long score, String value, boolean onlyIfAbsent) {
        var queue = store.putIfAbsent(key, new TreeMap<>()).sortedSet();
        if (!onlyIfAbsent) {
            queue.remove(value);
        }
        return queue.putIfAbsent(value, score) == null;
    }

    @Override
    public String popByScoreCap(String key, long maxScore) {
        MockRedisStore.Value value = store.get(key);
        if (value == null) return null;
        var queue = value.sortedSet();
        return queue.entrySet().stream()
                    .filter(entry -> entry.getValue() <= maxScore)
                    .sorted(Map.Entry.comparingByValue())
                    .limit(1)
                    .peek(entry -> queue.remove(entry.getKey()))
                    .map(Map.Entry::getKey)
                    .reduce(String::concat).orElse(null);
    }
}
