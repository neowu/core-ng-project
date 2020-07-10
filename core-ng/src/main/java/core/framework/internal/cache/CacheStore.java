package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.Validator;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public interface CacheStore {   // all keys here are direct cacheKey, not the key passed to Cache<T>
    <T> T get(String key, JSONMapper<T> mapper, Validator validator);

    <T> Map<String, T> getAll(String[] keys, JSONMapper<T> mapper, Validator validator);

    <T> void put(String key, T value, Duration expiration, JSONMapper<T> mapper);

    <T> void putAll(List<Entry<T>> values, Duration expiration, JSONMapper<T> mapper);

    boolean delete(String... keys);

    class Entry<T> {
        public final String key;
        public final T value;

        public Entry(String key, T value) {
            this.key = key;
            this.value = value;
        }
    }
}
