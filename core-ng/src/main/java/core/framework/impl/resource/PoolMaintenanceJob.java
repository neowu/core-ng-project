package core.framework.impl.resource;

import core.framework.api.log.ActionLogContext;
import core.framework.api.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class PoolMaintenanceJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(PoolMaintenanceJob.class);

    private final Pool<?> pool;

    public PoolMaintenanceJob(Pool<?> pool) {
        this.pool = pool;
    }

    @Override
    public void execute() throws Exception {
        ActionLogContext.put("pool", pool.name);

        int currentSize = pool.currentSize.get();
        logger.debug("currentPoolSize={}, minSize={}, maxSize={}", currentSize, pool.minSize, pool.maxSize);

        logger.debug("recycle idle items");
        pool.recycleIdleItems();

        logger.debug("replenish items");
        pool.replenish();
    }
}
