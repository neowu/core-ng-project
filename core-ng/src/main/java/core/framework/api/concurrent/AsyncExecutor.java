package core.framework.api.concurrent;

import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public class AsyncExecutor {
    private final Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);
    private final Executor executor;
    private final LogManager logManager;

    public AsyncExecutor(Executor executor, LogManager logManager) {
        this.executor = executor;
        this.logManager = logManager;
    }

    public <T> Future<T> submit(String name, Callable<T> task) {
        logger.debug("submit async task, name={}", name);

        ActionLog parentActionLog = logManager.currentActionLog();
        String refId = parentActionLog != null ? parentActionLog.refId() : null;
        String action = parentActionLog != null ? parentActionLog.action() + "/" + name : name;
        boolean trace = parentActionLog != null && parentActionLog.trace;

        return executor.submit(() -> {
            ActionLog currentActionLog = logManager.currentActionLog();
            currentActionLog.refId(refId);
            currentActionLog.action(action);
            if (trace) {
                currentActionLog.triggerTraceLog();
            }
            return task.call();
        });
    }
}
