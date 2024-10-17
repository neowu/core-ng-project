package core.framework.redis;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public interface RedisHash {
    @Nullable
    String get(String key, String field);

    Map<String, String> getAll(String key);

    void set(String key, String field, String value);

    void multiSet(String key, Map<String, String> values);

    long increaseBy(String key, String field, long increment);

    long del(String key, String... fields);

    // supported by redis since 7.4
    void expire(String key, String field, Duration duration);
}
