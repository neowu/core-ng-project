package core.framework.redis;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

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

    boolean del(String key);

    long increaseBy(String key, long increment);

    Map<String, String> multiGet(String... keys);

    void multiSet(Map<String, String> values);

    RedisHash hash();

    void forEach(String pattern, Consumer<String> consumer);
}
