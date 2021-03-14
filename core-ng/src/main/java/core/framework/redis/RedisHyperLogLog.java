package core.framework.redis;

/**
 * @author tempo
 */
public interface RedisHyperLogLog {
    boolean add(String key, String... values);

    long count(String... keys);
}
