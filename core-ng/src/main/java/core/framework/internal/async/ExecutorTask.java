package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.log.Trace;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author neo
 */
public class ExecutorTask<T> implements Callable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorTask.class);
    final String actionId;
    private final String action;
    private final LogManager logManager;
    private final Callable<T> task;
    private final Instant startTime;
    private final long maxProcessTimeInNano;
    private final String rootAction;
    private final String refId;
    private final String correlationId;
    private final Trace trace;
    private final Map<String, PerformanceWarning> warnings;

    ExecutorTask(Callable<T> task, LogManager logManager, TaskContext context) {
        this.task = task;
        this.logManager = logManager;
        actionId = context.actionId;
        action = context.action;
        startTime = context.startTime;
        maxProcessTimeInNano = context.maxProcessTimeInNano;
        ActionLog parentActionLog = context.parentActionLog;
        if (parentActionLog != null) {  // only keep info needed by call(), so parentActionLog can be GCed sooner
            List<String> parentActionContext = parentActionLog.context.get("root_action");
            rootAction = parentActionContext != null ? parentActionContext.get(0) : parentActionLog.action;
            correlationId = parentActionLog.correlationId();
            refId = parentActionLog.id;
            trace = parentActionLog.trace;
            warnings = parentActionLog.warningContext.warnings;
        } else {
            rootAction = null;
            correlationId = null;
            refId = null;
            trace = null;
            warnings = null;
        }
    }

    @Override
    public T call() throws Exception {
        try {
            ActionLog actionLog = logManager.begin("=== task execution begin ===", actionId);
            actionLog.action(action());
            actionLog.warningContext.maxProcessTimeInNano(maxProcessTimeInNano);
            // here it doesn't log task class, is due to task usually is lambda or method reference, it's expensive to inspect, refer to ControllerInspector
            if (rootAction != null) { // if rootAction != null, then all parent info are available
                actionLog.context("root_action", rootAction);
                LOGGER.debug("correlationId={}", correlationId);
                actionLog.correlationIds = List.of(correlationId);
                LOGGER.debug("refId={}", refId);
                actionLog.refIds = List.of(refId);
                if (trace == Trace.CASCADE) actionLog.trace = Trace.CASCADE;
                actionLog.warningContext.warnings = warnings;
            }
            LOGGER.debug("taskClass={}", CallableTask.taskClass(task).getName());
            Duration delay = Duration.between(startTime, actionLog.date);
            LOGGER.debug("taskDelay={}", delay);
            actionLog.stats.put("task_delay", (double) delay.toNanos());
            actionLog.context.put("thread", List.of(Thread.currentThread().getName()));
            return task.call();
        } catch (Throwable e) {
            logManager.logError(e);
            throw new TaskException(Strings.format("task failed, action={}, id={}, error={}", action, actionId, e.getMessage()), e);
        } finally {
            logManager.end("=== task execution end ===");
        }
    }

    String action() {
        return rootAction == null ? "task:" + action : rootAction + ":task:" + action;
    }

    // used to print all canceled tasks during shutdown
    @Override
    public String toString() {
        return action() + ":" + actionId;
    }

    static class TaskContext {
        String actionId;
        String action;
        Instant startTime;
        ActionLog parentActionLog;
        long maxProcessTimeInNano;
    }
}
