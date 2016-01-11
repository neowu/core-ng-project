package core.framework.impl.async;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;

import java.util.concurrent.Callable;

/**
 * @author neo
 */
public final class Task<T> implements Callable<T> {
    private final Callable<T> task;
    private final LogManager logManager;
    private final String action;
    private final String refId;
    private final boolean trace;

    public Task(Callable<T> task, LogManager logManager, String action, String refId, boolean trace) {
        this.task = task;
        this.logManager = logManager;
        this.action = action;
        this.refId = refId;
        this.trace = trace;
    }

    @Override
    public T call() throws Exception {
        try {
            logManager.begin("=== task execution begin ===");
            ActionLog currentActionLog = logManager.currentActionLog();
            currentActionLog.refId(refId);
            currentActionLog.action(action);
            if (trace) logManager.triggerTraceLog();
            return task.call();
        } catch (Throwable e) {
            logManager.logError(e);
            throw e;
        } finally {
            logManager.end("=== task execution end ===");
        }
    }
}
