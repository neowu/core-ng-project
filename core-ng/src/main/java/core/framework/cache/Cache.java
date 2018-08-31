package core.framework.cache;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @author neo
 */
public interface Cache<T> {
    T get(String key, Function<String, T> loader);

    Map<String, T> getAll(Collection<String> keys, Function<String, T> loader);

    void put(String key, T value);

    void putAll(Map<String, T> values);

    void evict(String key);

    void evictAll(Collection<String> keys);
}
