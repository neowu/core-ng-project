package core.framework.module;

import core.framework.internal.cache.CacheImpl;
import core.framework.internal.cache.RedisCacheStore;

/**
 * @author neo
 */
public class CacheStoreConfig {
    private final CacheImpl<?> cache;
    private final CacheConfig config;

    CacheStoreConfig(CacheImpl<?> cache, CacheConfig config) {
        this.cache = cache;
        this.config = config;
    }

    // for rarely changed data, or stale data is tolerated
    // or use kafka with custom groupId to evict keys
    // otherwise refresh can also be done by restarting service if there is emergence
    public void local() {
        if (cache.cacheStore instanceof RedisCacheStore) {
            cache.cacheStore = config.localCacheStore();
        }
    }
}
