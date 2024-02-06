package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.log.Trace;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author neo
 */
class ExecutorTask<T> implements Callable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorTask.class);

    private final Callable<T> task;
    private final LogManager logManager;
    private final TaskContext context;

    private final String rootAction;
    private final String correlationId;
    private final String refId;
    private final Trace trace;
    @Nullable
    private final PerformanceWarning[] warnings;

    ExecutorTask(Callable<T> task, LogManager logManager, TaskContext context, ActionLog parentActionLog) {
        this.task = task;
        this.logManager = logManager;
        this.context = context;
        if (parentActionLog != null) {  // only keep info needed by call(), so parentActionLog can be GCed sooner
            List<String> parentActionContext = parentActionLog.context.get("root_action");
            rootAction = parentActionContext != null ? parentActionContext.getFirst() : parentActionLog.action;
            correlationId = parentActionLog.correlationId();
            refId = parentActionLog.id;
            trace = parentActionLog.trace == Trace.CASCADE ? Trace.CASCADE : null;  // trace only with parent.cascade
            warnings = parentActionLog.warnings();
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
        VirtualThread.COUNT.increase();
        ActionLog actionLog = logManager.begin("=== task execution begin ===", context.actionId);
        try {
            actionLog.action(action());
            actionLog.warningContext.maxProcessTimeInNano(context.maxProcessTimeInNano);
            // here doesn't log task class, due to task usually is lambda or method reference, it's expensive to inspect, refer to ControllerInspector
            if (rootAction != null) { // if rootAction != null, then all parent info are available
                actionLog.context("root_action", rootAction);
                LOGGER.debug("correlationId={}", correlationId);
                actionLog.correlationIds = List.of(correlationId);
                LOGGER.debug("refId={}", refId);
                actionLog.refIds = List.of(refId);
                if (trace != null) actionLog.trace = trace;
                if (warnings != null) actionLog.initializeWarnings(warnings);
            }
            LOGGER.debug("taskClass={}", CallableTask.taskClass(task).getName());
            Duration delay = Duration.between(context.startTime, actionLog.date);
            LOGGER.debug("taskDelay={}", delay);
            actionLog.stats.put("task_delay", (double) delay.toNanos());
            actionLog.context.put("thread", List.of(Thread.currentThread().getName()));
            return task.call();
        } catch (Throwable e) {
            logManager.logError(e);
            throw new TaskException(Strings.format("task failed, action={}, id={}, error={}", context.action, context.actionId, e.getMessage()), e);
        } finally {
            logManager.end("=== task execution end ===");
            context.runningTasks.remove(toString());
            VirtualThread.COUNT.decrease();
        }
    }

    String action() {
        return rootAction == null ? "task:" + context.action : rootAction + ":task:" + context.action;
    }

    // used to print all canceled tasks during shutdown, used by both ScheduledExecutorService and VirtualThreadExecutor
    @Override
    public String toString() {
        return action() + ":" + context.actionId;
    }

    record TaskContext(String actionId, String action, Instant startTime, long maxProcessTimeInNano, Set<String> runningTasks) {
    }
}
