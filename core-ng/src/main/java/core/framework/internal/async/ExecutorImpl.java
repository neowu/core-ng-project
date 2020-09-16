package core.framework.internal.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
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

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class ExecutorImpl implements Executor {
    private final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);
    private final ExecutorService executor;
    private final LogManager logManager;
    private final String name;
    volatile ScheduledExecutorService scheduler;

    public ExecutorImpl(int poolSize, String name, LogManager logManager) {
        this.name = name;
        this.logManager = logManager;
        this.executor = ThreadPools.cachedThreadPool(poolSize, "executor" + (name == null ? "" : "-" + name) + "-");
    }

    public void shutdown() {
        logger.info("shutting down executor, name={}", name);
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
        if (!success) logger.warn("failed to terminate executor, name={}", name);
        else logger.info("executor stopped, name={}", name);
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        logger.debug("submit task, action={}", action);
        Callable<T> execution = execution(action, task);
        return submitTask(action, execution);
    }

    @Override
    public void submit(String action, Task task, Duration delay) {
        synchronized (this) {
            if (executor.isShutdown()) {
                logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action);    // with current executor impl, rejection only happens when shutdown
                return;
            }
            if (scheduler == null) {
                scheduler = ThreadPools.singleThreadScheduler("executor-scheduler" + (name == null ? "" : "-" + name) + "-");
            }
        }
        logger.debug("submit delayed task, action={}, delay={}", action, delay);
        scheduleDelayedTask(action, task, delay);
    }

    boolean scheduleDelayedTask(String action, Task task, Duration delay) {
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
            return true;
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action, e);
        }
        return false;
    }

    private <T> Future<T> submitTask(String action, Callable<T> execution) {
        try {
            return executor.submit(execution);
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action, e);
            return new CancelledFuture<>();
        }
    }

    private <T> Callable<T> execution(String action, Callable<T> task) {
        ActionLog parentActionLog = LogManager.CURRENT_ACTION_LOG.get();
        return new ExecutorTask<>(action, task, logManager, parentActionLog);
    }
}
