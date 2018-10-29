package core.framework.impl.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    public ExecutorImpl(ExecutorService executor, LogManager logManager, String name) {
        this.executor = executor;
        this.logManager = logManager;
        this.name = "executor" + (name == null ? "" : "-" + name);
    }

    public void shutdown() {
        logger.info("shutting down {}", name);
        synchronized (this) {
            if (scheduler != null) {
                List<Runnable> delayedTasks = scheduler.shutdownNow(); // drop all delayed tasks
                logger.info("stop {} delayed tasks, cancelled={}", name, delayedTasks.size());
            }
        }
        executor.shutdown();
    }

    public void awaitTermination(long timeoutInMs) throws InterruptedException {
        boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) logger.warn("failed to terminate {}", name);
        else logger.info("{} stopped", name);
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        Callable<T> execution = execution(action, task);
        return executor.submit(execution);
    }

    @Override
    public void submit(String action, Task task, Duration delay) {
        ScheduledExecutorService scheduler = scheduler();
        // construct execution outside scheduler thread, to obtain parent action log
        Callable<Void> execution = execution(action, () -> {
            task.execute();
            return null;
        });
        scheduler.schedule(() -> {
            try {
                executor.submit(execution);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);    // in scheduler.schedule exception will be silenced swallowed, here is to print if any
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private ScheduledExecutorService scheduler() {
        synchronized (this) {
            if (scheduler == null) scheduler = Executors.newSingleThreadScheduledExecutor();
            return scheduler;
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
