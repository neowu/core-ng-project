package core.framework.redis;

import java.util.Map;

/**
 * @author tempo
 */
public interface RedisSortedSet {
    boolean zadd(String key, String value, long score, boolean onlyIfAbsent);

    Map<String, Long> zrange(String key, long start, long end);

    Map<String, Long> zpopByScore(String key, long minScore, long maxScore, long limit);
}
