package core.framework.impl.cache;

import core.framework.api.cache.Cache;
import core.framework.api.util.JSON;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

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
    public T get(String key, Supplier<T> supplier) {
        String result = cacheStore.get(name, key);
        if (result == null) {
            T value = supplier.get();
            put(key, value);
            return value;
        }
        return JSON.fromJSON(valueType, result);
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
