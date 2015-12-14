package core.framework.api.concurrent;

import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public class AsyncExecutor {
    private final Executor executor;
    private final LogManager logManager;

    public AsyncExecutor(Executor executor, LogManager logManager) {
        this.executor = executor;
        this.logManager = logManager;
    }

    public <T> Future<T> submit(String name, Callable<T> task) {
        // do not log intentionally, so in batch pattern where one batch submit task for each item, the batch process won't reach max trace log line limit.
        ActionLog parentActionLog = logManager.currentActionLog();
        String refId = parentActionLog != null ? parentActionLog.refId() : null;
        String action = parentActionLog != null ? parentActionLog.action + "/" + name : name;
        boolean trace = parentActionLog != null && parentActionLog.trace;

        return executor.submit(() -> {
            ActionLog currentActionLog = logManager.currentActionLog();
            currentActionLog.refId(refId);
            currentActionLog.action(action);
            if (trace) {
                logManager.triggerTraceLog();
            }
            return task.call();
        });
    }
}
