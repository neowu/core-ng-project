package core.framework.impl.cache;

import java.time.Duration;

/**
 * @author neo
 */
public interface CacheStore {
    String get(String name, String key);

    void put(String name, String key, String value, Duration expiration);

    void delete(String name, String key);
}
