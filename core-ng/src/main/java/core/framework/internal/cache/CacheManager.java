package core.framework.internal.cache;

import core.framework.cache.Cache;
import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class CacheManager {
    private final Map<String, CacheImpl<?>> caches = Maps.newHashMap();

    public CacheStore localCacheStore;
    public CacheStore remoteCacheStore;

    public <T> Cache<T> add(Class<T> cacheClass, Duration duration, boolean localCache) {
        new CacheClassValidator(cacheClass).validate();

        String name = cacheName(cacheClass);
        CacheImpl<T> cache = new CacheImpl<>(name, cacheClass, duration, localCache ? localCacheStore : remoteCacheStore);
        CacheImpl<?> previous = caches.putIfAbsent(name, cache);
        if (previous != null) throw new Error("found duplicate cache name, name=" + name);
        return cache;
    }

    public Optional<CacheImpl<?>> get(String name) {
        return Optional.ofNullable(caches.get(name));
    }

    public List<CacheImpl<?>> caches() {
        return new ArrayList<>(caches.values());
    }

    String cacheName(Class<?> cacheClass) {
        return ASCII.toLowerCase(cacheClass.getSimpleName());
    }
}
