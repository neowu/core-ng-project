package core.framework.impl.cache;

import core.framework.scheduler.Job;

/**
 * @author neo
 */
public class CleanupLocalCacheStoreJob implements Job {
    private final LocalCacheStore cacheStore;

    public CleanupLocalCacheStoreJob(LocalCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public void execute() throws Exception {
        cacheStore.cleanup();
    }
}
