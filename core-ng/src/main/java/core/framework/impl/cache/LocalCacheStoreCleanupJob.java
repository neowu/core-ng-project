package core.framework.impl.cache;

import core.framework.api.scheduler.Job;

/**
 * @author neo
 */
public class LocalCacheStoreCleanupJob implements Job {
    private final LocalCacheStore cacheStore;

    public LocalCacheStoreCleanupJob(LocalCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public void execute() throws Exception {
        cacheStore.cleanup();
    }
}
