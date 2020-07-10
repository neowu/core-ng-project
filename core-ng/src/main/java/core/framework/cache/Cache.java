package core.framework.cache;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @author neo
 */
public interface Cache<T> {
    // loader must not return null, use wrapper class if it is necessary to cache null value
    // for performance reason, it does not copy the object returned by local cache, so it must not modify local cache object unless to put it back
    T get(String key, Function<String, T> loader);

    Map<String, T> getAll(Collection<String> keys, Function<String, T> loader);

    void put(String key, T value);

    void putAll(Map<String, T> values);

    void evict(String key);

    void evictAll(Collection<String> keys);
}
