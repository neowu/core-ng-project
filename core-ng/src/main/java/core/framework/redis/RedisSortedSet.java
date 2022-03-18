package core.framework.redis;

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

    default Map<String, Long> popMin(String key) {
        return popMin(key, 1);
    }

    Map<String, Long> popMin(String key, long limit);

    long removeRangeByScore(String key, long minScore, long maxScore);
}
