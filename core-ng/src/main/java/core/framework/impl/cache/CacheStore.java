package core.framework.impl.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public interface CacheStore {
    String get(String name, String key);

    List<String> getAll(String name, List<String> keys);

    void put(String name, String key, String value, Duration expiration);

    void putAll(String name, Map<String, String> values, Duration expiration);

    void delete(String name, String key);
}
