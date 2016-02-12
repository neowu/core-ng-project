package core.framework.impl.cache;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public interface CacheStore {   // all keys here are direct cacheKey, not the key passed to Cache<T>
    byte[] get(String key);

    Map<String, byte[]> getAll(String[] keys);

    void put(String key, byte[] value, Duration expiration);

    void putAll(Map<String, byte[]> values, Duration expiration);

    void delete(String key);
}
