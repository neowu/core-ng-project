package core.framework.internal.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.internal.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
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
    private final long maxProcessTimeInNano;
    volatile ScheduledExecutorService scheduler;

    public ExecutorImpl(int poolSize, String name, LogManager logManager, long maxProcessTimeInNano) {
        this.name = name;
        this.logManager = logManager;
        this.maxProcessTimeInNano = maxProcessTimeInNano;
        this.executor = ThreadPools.cachedThreadPool(poolSize, "executor" + (name == null ? "" : "-" + name) + "-");
    }

    public void shutdown() {
        logger.info("shutting down executor, name={}", name);
        synchronized (this) {
            if (scheduler != null) {
                List<Runnable> canceledTasks = scheduler.shutdownNow(); // drop all delayed tasks
                if (!canceledTasks.isEmpty()) {
                    logger.warn(errorCode("TASK_REJECTED"), "delayed tasks are canceled due to server is shutting down, name={}, tasks={}", name, canceledTasks);
                }
            }
            executor.shutdown();
        }
    }

    public void awaitTermination(long timeoutInMs) throws InterruptedException {
        boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) {
            List<Runnable> canceledTasks = executor.shutdownNow();    // only return tasks not started yet
            logger.warn(errorCode("FAILED_TO_STOP"), "failed to terminate executor, name={}, canceledTasks={}", name, canceledTasks);
        } else {
            logger.info("executor stopped, name={}", name);
        }
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        var now = Instant.now();
        String actionId = LogManager.ID_GENERATOR.next(now);
        logger.debug("submit task, action={}, id={}, taskClass={}", action, actionId, CallableTask.taskClass(task).getName());
        ExecutorTask<T> execution = execution(actionId, action, now, task);
        return submitTask(execution);
    }

    @Override
    public Future<Void> submit(String action, Task task) {
        // wrap task with class (rather than lambda), to preserve task class info
        return submit(action, new CallableTask(task));
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
        scheduleDelayedTask(action, task, delay);
    }

    boolean scheduleDelayedTask(String action, Task task, Duration delay) {
        var now = Instant.now();
        String actionId = LogManager.ID_GENERATOR.next(now);
        logger.debug("submit delayed task, action={}, id={}, delay={}", action, actionId, delay);

        // construct execution outside scheduler thread, to obtain parent action log
        ExecutorTask<Void> execution = execution(actionId, action, now.plus(delay), () -> {
            task.execute();
            return null;
        });
        try {
            scheduler.schedule(new DelayedTask(execution), delay.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action, e);
        }
        return false;
    }

    private <T> Future<T> submitTask(ExecutorTask<T> execution) {
        try {
            return executor.submit(execution);
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", execution.action(), e);
            return new CancelledFuture<>();
        }
    }

    private <T> ExecutorTask<T> execution(String actionId, String action, Instant startTime, Callable<T> task) {
        var context = new ExecutorTask.TaskContext();
        context.actionId = actionId;
        context.action = action;
        context.startTime = startTime;
        context.parentActionLog = LogManager.CURRENT_ACTION_LOG.get();
        context.maxProcessTimeInNano = maxProcessTimeInNano;
        return new ExecutorTask<>(task, logManager, context);
    }

    class DelayedTask implements Callable<Void> {
        final ExecutorTask<Void> execution;

        DelayedTask(ExecutorTask<Void> execution) {
            this.execution = execution;
        }

        @Override
        public Void call() {
            try {
                submitTask(execution);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);    // in scheduler.schedule exception will be swallowed, here is to log if any
            }
            return null;
        }

        // used to print all canceled tasks during shutdown
        @Override
        public String toString() {
            return execution.toString();
        }
    }
}
