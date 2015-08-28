package core.framework.impl.resource;

import core.framework.api.log.ActionLogContext;
import core.framework.api.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class RefreshPoolJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(RefreshPoolJob.class);

    private final Pool<?> pool;

    public RefreshPoolJob(Pool<?> pool) {
        this.pool = pool;
    }

    @Override
    public void execute() throws Exception {
        ActionLogContext.put("pool", pool.name);

        logger.debug("total={}, idle={}, minSize={}, maxSize={}", pool.total.get(), pool.idleItems.size(), pool.minSize, pool.maxSize);

        logger.debug("recycle idle items");
        pool.recycleIdleItems();

        logger.debug("replenish items");
        pool.replenish();

        ActionLogContext.put("total", pool.total.get());
        ActionLogContext.put("idle", pool.idleItems.size());
    }
}
