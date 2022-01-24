package core.framework.internal.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public interface CacheStore {   // all keys here are direct cacheKey, not the key passed to Cache<T>
    <T> T get(String key, CacheContext<T> context);

    <T> Map<String, T> getAll(String[] keys, CacheContext<T> context);

    <T> void put(String key, T value, Duration expiration, CacheContext<T> context);

    <T> void putAll(List<Entry<T>> values, Duration expiration, CacheContext<T> context);

    boolean delete(String... keys);

    record Entry<T>(String key, T value) {
    }
}
