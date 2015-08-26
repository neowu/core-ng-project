package core.framework.impl.resource;

import core.framework.api.log.ActionLogContext;
import core.framework.api.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class PoolResizeJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(PoolResizeJob.class);

    private final Pool<?> pool;

    public PoolResizeJob(Pool<?> pool) {
        this.pool = pool;
    }

    @Override
    public void execute() throws Exception {
        ActionLogContext.put("pool", pool.name);

        int currentSize = pool.currentSize.get();
        logger.debug("currentPoolSize={}, minSize={}, maxSize={}", currentSize, pool.minSize, pool.maxSize);

        if (currentSize > pool.minSize) {
            pool.recycleIdleItems();
        } else if (currentSize < pool.minSize) {
            logger.debug("replenish pool");
            pool.replenish();
        } else {
            logger.debug("skip, pool size is equal to min size");
        }
    }
}
