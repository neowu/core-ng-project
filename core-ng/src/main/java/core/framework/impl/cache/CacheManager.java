package core.framework.impl.cache;

import core.framework.cache.Cache;
import core.framework.util.Maps;
import core.framework.util.Strings;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class CacheManager {
    private final CacheStore cacheStore;
    private final Map<String, CacheImpl<?>> caches = Maps.newHashMap();

    public CacheManager(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    public <T> Cache<T> add(String name, Type valueType, Duration duration) {
        new CacheTypeValidator(valueType).validate();

        CacheImpl<T> cache = new CacheImpl<>(name, valueType, duration, cacheStore);
        CacheImpl<?> previous = caches.putIfAbsent(name, cache);
        if (previous != null) throw new Error(Strings.format("found duplicate cache name, name={}", name));
        return cache;
    }

    public Optional<CacheImpl<?>> get(String name) {
        return Optional.ofNullable(caches.get(name));
    }

    public List<CacheImpl<?>> caches() {
        return new ArrayList<>(caches.values());
    }
}
