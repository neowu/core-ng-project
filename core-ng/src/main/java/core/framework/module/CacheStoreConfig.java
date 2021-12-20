package core.framework.module;

import core.framework.internal.cache.CacheImpl;
import core.framework.internal.cache.RedisCacheStore;
import core.framework.internal.cache.RedisLocalCacheStore;

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

    public void local() {
        if (cache.cacheStore instanceof RedisCacheStore) {
            cache.cacheStore = config.redisLocalCacheStore();
        }
    }

    // for rarely changed data, or tolerate stale data,
    // in microservice env, only way to refresh is expiration or restart service
    public void localOnly() {
        if (cache.cacheStore instanceof RedisCacheStore || cache.cacheStore instanceof RedisLocalCacheStore) {
            cache.cacheStore = config.localCacheStore();
        }
    }
}
