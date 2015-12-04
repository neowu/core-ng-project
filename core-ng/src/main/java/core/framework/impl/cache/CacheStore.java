package core.framework.impl.cache;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public interface CacheStore {   // all keys here are direct cacheKey, not the key passed to Cache<T>
    String get(String key);

    Map<String, String> getAll(String[] keys);

    void put(String key, String value, Duration expiration);

    void delete(String key);
}
