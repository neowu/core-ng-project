package core.framework.api.cache;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author neo
 */
public interface Cache<T> {
    T get(String key, Function<String, T> loader);

    Map<String, T> getAll(List<String> keys, Function<String, T> loader);

    void put(String key, T value);

    void evict(String key);
}
