package core.framework.impl.cache;

import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public interface CacheStore {
    String get(String name, String key);

    List<String> getAll(String name, List<String> keys);

    void put(String name, String key, String value, Duration expiration);

    void delete(String name, String key);
}
