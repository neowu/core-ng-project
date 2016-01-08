package core.framework.api.concurrent;

import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;

import java.util.concurrent.Semaphore;

/**
 * @author neo
 */
public final class Batch implements AutoCloseable {
    private final Executor executor;
    private final LogManager logManager;
    private final Semaphore semaphore;

    private final String action;
    private final String refId;
    private final boolean trace;
    private final int maxConcurrentHandlers;

    public Batch(String name, Executor executor, LogManager logManager) {
        this.executor = executor;
        this.logManager = logManager;
        maxConcurrentHandlers = Runtime.getRuntime().availableProcessors() * 4;
        semaphore = new Semaphore(maxConcurrentHandlers);
        ActionLog parentActionLog = logManager.currentActionLog();
        action = parentActionLog != null ? parentActionLog.action + "/" + name : name;  // parent action can be null in unit test context
        refId = parentActionLog != null ? parentActionLog.refId() : null;
        trace = parentActionLog != null && parentActionLog.trace;
    }

    public void submit(Task task) {
        // do not log intentionally, so in batch pattern where one batch submit task for each item, the batch process won't reach max trace log line limit.
        semaphore.acquireUninterruptibly(); // only be interrupted by shutdown
        executor.submit(() -> {
            try {
                process(task);
                return null;
            } finally {
                semaphore.release();
            }
        });
    }

    @Override
    public void close() {
        semaphore.acquireUninterruptibly(maxConcurrentHandlers);    // only be interrupted by shutdown
    }

    private void process(Task task) throws Exception {
        ActionLog currentActionLog = logManager.currentActionLog();
        currentActionLog.refId(refId);
        currentActionLog.action(action);
        if (trace) {
            logManager.triggerTraceLog();
        }
        task.execute();
    }
}
