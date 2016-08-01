package core.framework.api.redis;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public interface Redis {
    String get(String key);

    void set(String key, String value);

    void set(String key, String value, Duration expiration);

    boolean setIfAbsent(String key, String value, Duration expiration);

    void expire(String key, Duration duration);

    void del(String key);

    Map<String, String> mget(String... keys);

    void mset(Map<String, String> values);

    String hget(String key, String field);

    Map<String, String> hgetAll(String key);

    void hset(String key, String field, String value);

    void hmset(String key, Map<String, String> values);
}
