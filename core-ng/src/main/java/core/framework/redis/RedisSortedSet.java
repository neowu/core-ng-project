package core.framework.redis;

import java.util.Map;

/**
 * @author tempo
 */
public interface RedisSortedSet {
    // use long as score to keep precision, redis uses float type for score
    boolean add(String key, String value, long score, boolean onlyIfAbsent);

    default Map<String, Long> range(String key) {
        return range(key, 0, -1);
    }

    Map<String, Long> range(String key, long start, long stop);

    default Map<String, Long> popByScore(String key, long minScore, long maxScore) {
        return popByScore(key, minScore, maxScore, -1);
    }

    Map<String, Long> popByScore(String key, long minScore, long maxScore, long limit);
}
