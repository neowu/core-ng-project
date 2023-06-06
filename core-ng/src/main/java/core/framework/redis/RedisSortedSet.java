package core.framework.redis;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author tempo
 */
public interface RedisSortedSet {
    // use long as score to keep precision, redis uses float type for score
    default boolean add(String key, String value, long score) {
        return add(key, Map.of(value, score), false) == 1;
    }

    int add(String key, Map<String, Long> values, boolean onlyIfAbsent);

    long increaseScoreBy(String key, String value, long increment);

    default Map<String, Long> range(String key) {
        return range(key, 0, -1);
    }

    Map<String, Long> range(String key, long start, long stop);

    default Map<String, Long> rangeByScore(String key, long minScore, long maxScore) {
        return rangeByScore(key, minScore, maxScore, -1);
    }

    Map<String, Long> rangeByScore(String key, long minScore, long maxScore, long limit);

    default Map<String, Long> popByScore(String key, long minScore, long maxScore) {
        return popByScore(key, minScore, maxScore, -1);
    }

    Map<String, Long> popByScore(String key, long minScore, long maxScore, long limit);

    @Nullable
    default String popMin(String key) {
        Map<String, Long> values = popMin(key, 1);
        Iterator<String> iterator = values.keySet().iterator();
        if (iterator.hasNext()) return iterator.next();
        return null;
    }

    Map<String, Long> popMin(String key, long limit);

    long remove(String key, String... values);
}
