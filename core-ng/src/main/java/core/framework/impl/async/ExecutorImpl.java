package core.framework.impl.async;

import core.framework.async.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class ExecutorImpl implements Executor {
    private final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);
    private final ExecutorService executor;
    private final LogManager logManager;
    private final String name;

    public ExecutorImpl(ExecutorService executor, LogManager logManager, String name) {
        this.executor = executor;
        this.logManager = logManager;
        this.name = "executor" + (name == null ? "" : "-" + name);
    }

    public void shutdown(long timeoutInMs) throws InterruptedException {
        logger.info("shutting down {}", name);
        executor.shutdown();
        boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) logger.warn("failed to terminate {}", name);
        else logger.info("{} stopped", name);
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        ActionLog parentActionLog = LogManager.CURRENT_ACTION_LOG.get();
        String taskAction = taskAction(action, parentActionLog.action);
        String correlationId = parentActionLog.correlationId();
        String refId = parentActionLog.id;
        boolean trace = parentActionLog.trace;
        return executor.submit(() -> {
            try {
                ActionLog actionLog = logManager.begin("=== task execution begin ===");
                actionLog.action(taskAction);
                logger.debug("correlationId={}", correlationId);
                actionLog.correlationIds = List.of(correlationId);
                logger.debug("refId={}", refId);
                actionLog.refIds = List.of(refId);
                actionLog.trace = trace;
                return task.call();
            } catch (Throwable e) {
                logManager.logError(e);
                throw e;
            } finally {
                logManager.end("=== task execution end ===");
            }
        });
    }

    String taskAction(String action, String parentAction) {
        String postfix = ":" + action;
        if (parentAction.endsWith(postfix)) return parentAction;
        return parentAction + postfix;
    }
}
