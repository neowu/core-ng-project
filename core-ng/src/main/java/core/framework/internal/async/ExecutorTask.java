package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author neo
 */
public class ExecutorTask<T> implements Callable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorTask.class);
    private final String action;
    private final LogManager logManager;
    private final Callable<T> task;
    private final Instant startTime;
    private final String actionId;
    private String rootAction;
    private String refId;
    private String correlationId;
    private boolean trace;

    ExecutorTask(String actionId, String action, Instant startTime, ActionLog parentActionLog, LogManager logManager, Callable<T> task) {
        this.action = action;
        this.task = task;
        this.logManager = logManager;
        this.startTime = startTime;
        this.actionId = actionId;
        if (parentActionLog != null) {  // only keep info needed by call(), so parentActionLog can be GCed sooner
            List<String> parentActionContext = parentActionLog.context.get("root_action");
            rootAction = parentActionContext != null ? parentActionContext.get(0) : parentActionLog.action;
            correlationId = parentActionLog.correlationId();
            refId = parentActionLog.id;
            trace = parentActionLog.trace;
        }
    }

    @Override
    public T call() throws Exception {
        try {
            ActionLog actionLog = logManager.begin("=== task execution begin ===", actionId);
            actionLog.action(action());
            // here it doesn't log task class, is due to task usually is lambda or method reference, it's expensive to inspect, refer to ControllerInspector
            if (rootAction != null) { // if rootAction != null, then all parent info are available
                actionLog.context("root_action", rootAction);
                LOGGER.debug("correlationId={}", correlationId);
                actionLog.correlationIds = List.of(correlationId);
                LOGGER.debug("refId={}", refId);
                actionLog.refIds = List.of(refId);
                actionLog.trace = trace;
            }
            Duration delay = Duration.between(startTime, actionLog.date);
            LOGGER.debug("[stat] task_delay={}", delay);
            actionLog.stats.put("task_delay", (double) delay.toNanos());
            return task.call();
        } catch (Throwable e) {
            logManager.logError(e);
            throw new TaskException(Strings.format("task failed, action={}, id={}, error={}", action, actionId, e.getMessage()), e);
        } finally {
            logManager.end("=== task execution end ===");
        }
    }

    String action() {
        return rootAction == null ? "task:" + action : rootAction + ":" + action;
    }
}
