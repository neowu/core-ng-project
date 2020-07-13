package core.framework.internal.cache;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;

/**
 * @author neo
 */
public class LocalCacheMetrics implements Metrics {
    private final LocalCacheStore cacheStore;

    public LocalCacheMetrics(LocalCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public void collect(Stats stats) {
        stats.put("cache_size", cacheStore.caches.size());
    }
}
