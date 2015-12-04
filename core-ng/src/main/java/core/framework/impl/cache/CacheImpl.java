package core.framework.impl.cache;

import core.framework.api.cache.Cache;
import core.framework.api.util.JSON;

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
        String cacheKey = cacheKey(key);
        String cacheValue = cacheStore.get(cacheKey);
        if (cacheValue == null) {
            T value = loader.apply(key);
            cacheStore.put(cacheKey, JSON.toJSON(value), duration);
            return value;
        }
        return JSON.fromJSON(valueType, cacheValue);
    }

    @Override
    public List<T> getAll(List<String> keys, Function<String, T> loader) {
        int size = keys.size();
        String[] cacheKeys = new String[size];
        int index = 0;
        for (String key : keys) {
            cacheKeys[index] = cacheKey(key);
            index++;
        }
        List<T> values = new ArrayList<>(size);
        Map<String, String> cacheValues = cacheStore.getAll(cacheKeys);
        index = 0;
        for (String key : keys) {
            String cacheKey = cacheKeys[index];
            String cacheValue = cacheValues.get(cacheKey);
            if (cacheValue == null) {
                T value = loader.apply(key);
                cacheStore.put(cacheKey, JSON.toJSON(value), duration);
                values.add(value);
            } else {
                values.add(JSON.fromJSON(valueType, cacheValue));
            }
            index++;
        }
        return values;
    }

    @Override
    public void put(String key, T value) {
        cacheStore.put(cacheKey(key), JSON.toJSON(value), duration);
    }

    @Override
    public void evict(String key) {
        cacheStore.delete(cacheKey(key));
    }

    public Optional<String> get(String key) {
        String result = cacheStore.get(cacheKey(key));
        return Optional.ofNullable(result);
    }

    private String cacheKey(String key) {
        return name + ":" + key;
    }
}
