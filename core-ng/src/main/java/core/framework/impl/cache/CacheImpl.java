package core.framework.impl.cache;

import core.framework.api.cache.Cache;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author neo
 */
public class CacheImpl<T> implements Cache<T> {
    public final String name;
    public final Type valueType;
    public final Duration duration;
    private final CacheStore cacheStore;

    public CacheImpl(String name, Type valueType, Duration duration, CacheStore cacheStore) {
        this.name = name;
        this.valueType = valueType;
        this.duration = duration;
        this.cacheStore = cacheStore;
    }

    @Override
    public T get(String key, Function<String, T> loader) {
        String cacheValue = cacheStore.get(name, key);
        if (cacheValue == null) {
            T value = loader.apply(key);
            put(key, value);
            return value;
        }
        return JSON.fromJSON(valueType, cacheValue);
    }

    @Override
    public List<T> getAll(List<String> keys, Function<String, T> loader) {
        int size = keys.size();
        List<T> values = new ArrayList<>(size);
        Map<String, String> cacheValues = cacheStore.getAll(name, keys);
        Map<String, String> newValues = Maps.newHashMapWithExpectedSize(size);
        for (String key : keys) {
            String cacheValue = cacheValues.get(key);
            if (cacheValue == null) {
                T value = loader.apply(key);
                newValues.put(key, JSON.toJSON(value));
                values.add(value);
            } else {
                values.add(JSON.fromJSON(valueType, cacheValue));
            }
        }

        if (!newValues.isEmpty()) cacheStore.putAll(name, newValues, duration);
        return values;
    }

    @Override
    public void put(String key, T value) {
        cacheStore.put(name, key, JSON.toJSON(value), duration);
    }

    @Override
    public void evict(String key) {
        cacheStore.delete(name, key);
    }

    public Optional<String> get(String key) {
        String result = cacheStore.get(name, key);
        return Optional.ofNullable(result);
    }
}
