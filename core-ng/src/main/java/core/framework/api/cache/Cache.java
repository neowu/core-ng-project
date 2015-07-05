package core.framework.api.cache;

import java.util.function.Supplier;

/**
 * @author neo
 */
public interface Cache<T> {
    T get(String key, Supplier<T> supplier);

    void put(String key, T value);

    void evict(String key);
}
