package core.framework.module;

import core.framework.internal.cache.CacheImpl;
import core.framework.internal.cache.RedisCacheStore;

/**
 * @author neo
 */
public class CacheStoreConfig {
    private final CacheImpl<?> cache;
    private final CacheConfig config;

    public CacheStoreConfig(CacheImpl<?> cache, CacheConfig config) {
        this.cache = cache;
        this.config = config;
    }

    public void local() {
        if (cache.cacheStore instanceof RedisCacheStore) {
            cache.cacheStore = config.redisLocalCacheStore();
        }
    }
}
