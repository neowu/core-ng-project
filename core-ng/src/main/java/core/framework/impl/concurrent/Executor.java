package core.framework.impl.concurrent;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public class Executor {
    private final Logger logger = LoggerFactory.getLogger(Executor.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final LogManager logManager;

    public Executor(LogManager logManager) {
        this.logManager = logManager;
    }

    public void shutdown() {
        logger.info("shutdown executor");
        executorService.shutdown();
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(() -> execute(task));
    }

    private <T> T execute(Callable<T> task) {
        logManager.start();
        try {
            logger.debug("=== task execution begin ===");
            return task.call();
        } catch (Throwable e) {
            ActionLog actionLog = logManager.currentActionLog();
            actionLog.error(e);
            return null;
        } finally {
            logger.debug("=== task execution end ===");
            logManager.end();
        }
    }
}
