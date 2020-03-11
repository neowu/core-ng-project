package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author neo
 */
public class ExecutorTask<T> implements Callable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorTask.class);
    final String action;
    private final LogManager logManager;
    private final Callable<T> task;
    private String rootAction;
    private String refId;
    private String correlationId;
    private boolean trace;

    ExecutorTask(String action, Callable<T> task, LogManager logManager, ActionLog parentActionLog) {
        this.logManager = logManager;
        this.task = task;
        if (parentActionLog != null) {  // only keep info needed by call(), so parentActionLog can be GCed sooner
            List<String> parentActionContext = parentActionLog.context.get("root_action");
            rootAction = parentActionContext != null ? parentActionContext.get(0) : parentActionLog.action;
            correlationId = parentActionLog.correlationId();
            refId = parentActionLog.id;
            trace = parentActionLog.trace;
        }
        this.action = rootAction == null ? "task:" + action : rootAction + ":" + action;
    }

    @Override
    public T call() throws Exception {
        try {
            ActionLog actionLog = logManager.begin("=== task execution begin ===");
            actionLog.action(action);
            // here it doesn't log task class, is due to task usually is lambda or method reference, it takes big overhead to inspect, refer to ControllerInspector
            if (rootAction != null) { // if rootAction != null, then all parent info are available
                actionLog.context("root_action", rootAction);
                LOGGER.debug("correlationId={}", correlationId);
                actionLog.correlationIds = List.of(correlationId);
                LOGGER.debug("refId={}", refId);
                actionLog.refIds = List.of(refId);
                actionLog.trace = trace;
            }
            return task.call();
        } catch (Throwable e) {
            logManager.logError(e);
            throw e;
        } finally {
            logManager.end("=== task execution end ===");
        }
    }
}
