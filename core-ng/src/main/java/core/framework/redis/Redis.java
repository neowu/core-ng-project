package core.framework.redis;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author neo
 */
public interface Redis {
    @Nullable
    String get(String key);

    default void set(String key, String value) {
        set(key, value, null, false);
    }

    default void set(String key, String value, Duration expiration) {
        set(key, value, expiration, false);
    }

    boolean set(String key, String value, @Nullable Duration expiration, boolean onlyIfAbsent);

    RedisSet set();

    void expire(String key, Duration duration);

    long del(String... keys);

    long increaseBy(String key, long increment);

    Map<String, String> multiGet(String... keys);

    void multiSet(Map<String, String> values);

    void forEach(String pattern, Consumer<String> consumer);

    RedisHash hash();

    RedisList list();

    RedisSortedSet sortedSet();

    RedisAdmin admin();

    RedisHyperLogLog hyperLogLog();
}
