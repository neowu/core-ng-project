package core.framework.internal.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.log.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class ExecutorImpl implements Executor {
    private final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);
    private final ExecutorService executor;
    private final LogManager logManager;
    private final String name;
    private volatile ScheduledExecutorService scheduler;

    public ExecutorImpl(int poolSize, String name, LogManager logManager) {
        this.name = "executor" + (name == null ? "" : "-" + name);
        this.executor = ThreadPools.cachedThreadPool(poolSize, this.name + "-");
        this.logManager = logManager;
    }

    public void shutdown() {
        logger.info("shutting down {}", name);
        synchronized (this) {
            if (scheduler != null) {
                List<Runnable> delayedTasks = scheduler.shutdownNow(); // drop all delayed tasks
                logger.info("cancelled delayed tasks, name={}, cancelled={}", name, delayedTasks.size());
            }
            executor.shutdown();
        }
    }

    public void awaitTermination(long timeoutInMs) throws InterruptedException {
        boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) logger.warn("failed to terminate {}", name);
        else logger.info("{} stopped", name);
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        Callable<T> execution = execution(action, task);
        return submitTask(action, execution);
    }

    @Override
    public void submit(String action, Task task, Duration delay) {
        synchronized (this) {
            if (executor.isShutdown()) {
                logger.warn(Markers.errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action);    // with current executor impl, rejection only happens when shutdown
                return;
            }
            if (scheduler == null) {
                scheduler = ThreadPools.singleThreadScheduler(name + "-scheduler-");
            }
        }
        // construct execution outside scheduler thread, to obtain parent action log
        Callable<Void> execution = execution(action, () -> {
            task.execute();
            return null;
        });
        Runnable delayedTask = () -> {
            try {
                submitTask(action, execution);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);    // in scheduler.schedule exception will be swallowed, here is to log if any
            }
        };
        try {
            scheduler.schedule(delayedTask, delay.toMillis(), TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(Markers.errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action, e);
        }
    }

    private <T> Future<T> submitTask(String action, Callable<T> execution) {
        try {
            return executor.submit(execution);
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(Markers.errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action, e);
            return new CancelledFuture<>();
        }
    }

    String taskAction(String action, String parentAction) {
        String postfix = ":" + action;
        if (parentAction.endsWith(postfix)) return parentAction;
        return parentAction + postfix;
    }

    private <T> Callable<T> execution(String action, Callable<T> task) {
        ActionLog parentActionLog = LogManager.CURRENT_ACTION_LOG.get();
        String taskAction = taskAction(action, parentActionLog.action);
        String correlationId = parentActionLog.correlationId();
        String refId = parentActionLog.id;
        boolean trace = parentActionLog.trace;
        return () -> {
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
        };
    }
}
