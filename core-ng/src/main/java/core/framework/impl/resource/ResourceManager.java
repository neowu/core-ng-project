package core.framework.impl.resource;

import core.framework.api.util.Lists;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class ResourceManager {
    private final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private final List<Pool<?>> pools = Lists.newArrayList();
    private final LogManager logManager;
    private ScheduledExecutorService scheduler;

    public ResourceManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public void add(Pool<?> pool) {
        pools.add(pool);
    }

    public void initialize() {
        if (pools.isEmpty()) return;

        logger.info("initialize resources");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.submit(this::initializeResources);
        scheduler.scheduleWithFixedDelay(this::clearIdleResources, 5, 5, TimeUnit.MINUTES);
    }

    private void initializeResources() {
        Thread.currentThread().setName("resource-manager");
        logManager.start("=== initialize resources begin ===");
        ActionLog actionLog = logManager.currentActionLog();
        actionLog.action("task/initialize-resources");
        try {
            initializePools();
        } finally {
            logManager.end("=== initialize resources end ===");
        }
    }

    private void initializePools() {
        for (Pool<?> pool : pools) {
            try {
                logger.debug("initialize resource, resourceClass={}", pool.getClass().getCanonicalName());
                pool.initialize();
            } catch (Throwable e) {
                logManager.logError(e);
            }
        }
    }

    private void clearIdleResources() {
        logManager.start("=== clear idle resources begin ===");
        ActionLog actionLog = logManager.currentActionLog();
        actionLog.action("task/clear-idle-resources");
        try {
            clearPools();
        } finally {
            logManager.end("=== clear idle resources end ===");
        }
    }

    private void clearPools() {
        for (Pool<?> resource : pools) {
            try {
                logger.debug("clear resource, resourceClass={}", resource.getClass().getCanonicalName());
                resource.clearIdleItems();
            } catch (Throwable e) {
                logManager.logError(e);
            }
        }
    }

    public void shutdown() {
        if (pools.isEmpty()) return;
        logger.info("close resources");
        scheduler.shutdown();
        pools.forEach(Pool::close);
    }
}
