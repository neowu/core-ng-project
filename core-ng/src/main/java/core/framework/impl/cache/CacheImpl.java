package core.framework.impl.cache;

import core.framework.api.cache.Cache;
import core.framework.api.util.JSON;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        Iterator<String> keyIterator = keys.iterator();
        Iterator<String> cacheValueIterator = cacheStore.getAll(name, keys).iterator();
        List<T> values = new ArrayList<>(keys.size());

        while (true) {
            if (!cacheValueIterator.hasNext()) break;
            String cacheValue = cacheValueIterator.next();
            String key = keyIterator.next();

            if (cacheValue == null) {
                T value = loader.apply(key);
                put(key, value);
                values.add(value);
            } else {
                values.add(JSON.fromJSON(valueType, cacheValue));
            }
        }

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
