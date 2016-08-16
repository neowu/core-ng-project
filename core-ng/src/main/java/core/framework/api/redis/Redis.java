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

    RedisSet set();

    boolean setIfAbsent(String key, String value, Duration expiration);

    void expire(String key, Duration duration);

    void del(String key);

    Map<String, String> multiGet(String... keys);

    void multiSet(Map<String, String> values);

    RedisHash hash();
}
