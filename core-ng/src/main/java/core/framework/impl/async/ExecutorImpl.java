package core.framework.impl.async;

import core.framework.async.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class ExecutorImpl implements Executor {
    private final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);
    private final ExecutorService executor = ThreadPools.cachedThreadPool(Threads.availableProcessors() * 2, "executor-");
    private final LogManager logManager;

    public ExecutorImpl(LogManager logManager) {
        this.logManager = logManager;
    }

    public void stop() {
        logger.info("stop executor");
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);     // wait 10 seconds to finish current tasks
        } catch (InterruptedException e) {
            logger.warn("failed to wait all tasks to finish", e);
        }
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        ActionLog parentActionLog = logManager.currentActionLog();
        String taskAction = parentActionLog.action + ":" + action;
        String refId = parentActionLog.refId();
        boolean trace = parentActionLog.trace;
        return executor.submit(() -> {
            try {
                ActionLog actionLog = logManager.begin("=== task execution begin ===");
                actionLog.refId(refId);
                actionLog.action(taskAction);
                if (trace) {
                    actionLog.trace = true;
                }
                return task.call();
            } catch (Throwable e) {
                logManager.logError(e);
                throw e;
            } finally {
                logManager.end("=== task execution end ===");
            }
        });
    }
}
