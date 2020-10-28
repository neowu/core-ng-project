package core.framework.redis;

/**
 * @author tempo
 */
public interface RedisSortedSet {
    boolean push(String key, long score, String value, boolean onlyIfAbsent);

    String popByScoreCap(String key, long maxScore);
}
