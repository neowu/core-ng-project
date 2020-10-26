package core.framework.internal.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
                List<Runnable> canceledTasks = scheduler.shutdownNow(); // drop all delayed tasks
                if (!canceledTasks.isEmpty()) {
                    String tasks = canceledTasks.stream().map(canceledTask -> {
                        DelayedTask task = callableFromFutureTask((FutureTask<?>) canceledTask);
                        return task == null ? null : task.execution.action() + ":" + task.execution.actionId;
                    }).collect(Collectors.joining(", "));
                    logger.warn(errorCode("TASK_REJECTED"), "delayed tasks are canceled due to server is shutting down, name={}, tasks={}", name, tasks);
                }
            }
            executor.shutdown();
        }
    }

    public void awaitTermination(long timeoutInMs) throws InterruptedException {
        boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) {
            List<Runnable> canceledTasks = executor.shutdownNow();    // only return tasks not started yet
            String tasks = canceledTasks.stream().map(canceledTask -> {
                ExecutorTask<?> task = callableFromFutureTask((FutureTask<?>) canceledTask);
                return task == null ? null : task.action() + ":" + task.actionId;
            }).collect(Collectors.joining(", "));
            logger.warn(errorCode("FAILED_TO_STOP"), "failed to terminate executor, name={}, canceledTasks={}", name, tasks);
        } else {
            logger.info("executor stopped, name={}", name);
        }
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        Instant now = Instant.now();
        String actionId = LogManager.ID_GENERATOR.next(now);
        logger.debug("submit task, action={}, id={}", action, actionId);
        ExecutorTask<T> execution = execution(actionId, action, now, task);
        return submitTask(execution);
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
        Instant now = Instant.now();
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
        ActionLog parentActionLog = LogManager.CURRENT_ACTION_LOG.get();
        return new ExecutorTask<>(actionId, action, startTime, parentActionLog, logManager, task);
    }

    @SuppressWarnings("unchecked")
    private <T> T callableFromFutureTask(FutureTask<?> runnable) {
        try {
            Field field = FutureTask.class.getDeclaredField("callable");
            if (!field.trySetAccessible()) return null;
            return (T) field.get(runnable);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
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
    }
}
