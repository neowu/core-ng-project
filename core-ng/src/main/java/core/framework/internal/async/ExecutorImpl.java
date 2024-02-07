package core.framework.internal.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class ExecutorImpl implements Executor {
    private final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);
    private final ExecutorService executor;
    private final LogManager logManager;
    private final long maxProcessTimeInNano;
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> runningTasks = Sets.newConcurrentHashSet();     // track running tasks, used to print tasks failed to complete on shutdown

    volatile ScheduledExecutorService scheduler;

    public ExecutorImpl(ExecutorService executor, LogManager logManager, long maxProcessTimeInNano) {
        this.executor = executor;
        this.logManager = logManager;
        this.maxProcessTimeInNano = maxProcessTimeInNano;
    }

    public void shutdown() {
        logger.info("shutting down executor");
        lock.lock();
        try {
            if (scheduler != null) {
                List<Runnable> canceledTasks = scheduler.shutdownNow(); // drop all delayed tasks
                if (!canceledTasks.isEmpty()) {
                    logger.warn(errorCode("TASK_REJECTED"), "delayed tasks are canceled due to server is shutting down, tasks={}", canceledTasks);
                }
            }
            executor.shutdown();
        } finally {
            lock.unlock();
        }
    }

    public void awaitTermination(long timeoutInMs) throws InterruptedException {
        boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) {
            executor.shutdownNow();
            if (!runningTasks.isEmpty()) {
                logger.error(errorCode("FAILED_TO_STOP"), "failed to terminate executor, canceledTasks={}", runningTasks);
            }
        }
        logger.info("executor stopped");
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
        lock.lock();
        try {
            if (executor.isShutdown()) {
                logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action);    // with current executor impl, rejection only happens when shutdown
                return;
            }
            if (scheduler == null) {
                scheduler = ThreadPools.singleThreadScheduler("executor-scheduler-");
            }
        } finally {
            lock.unlock();
        }
        scheduleDelayedTask(action, task, delay);
    }

    boolean scheduleDelayedTask(String action, Task task, Duration delay) {
        var now = Instant.now();
        String actionId = LogManager.ID_GENERATOR.next(now);
        logger.debug("submit delayed task, action={}, id={}, delay={}", action, actionId, delay);

        // construct execution outside scheduler thread, to obtain parent action log
        ExecutorTask<Void> execution = execution(actionId, action, now.plus(delay), new CallableTask(task));
        try {
            scheduler.schedule(new DelayedTask(execution), delay.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", action, e);
        }
        return false;
    }

    private <T> Future<T> submitTask(ExecutorTask<T> execution) {
        String task = execution.toString();     // task is action:actionId which is unique
        try {
            runningTasks.add(task);
            return executor.submit(execution);
        } catch (RejectedExecutionException e) {    // with current executor impl, rejection only happens when shutdown
            logger.warn(errorCode("TASK_REJECTED"), "reject task due to server is shutting down, action={}", execution.action(), e);
            runningTasks.remove(task);
            return new CancelledFuture<>();
        }
    }

    private <T> ExecutorTask<T> execution(String actionId, String action, Instant startTime, Callable<T> task) {
        var context = new ExecutorTask.TaskContext(actionId, action, startTime, maxProcessTimeInNano, runningTasks);
        ActionLog parentActionLog = LogManager.CURRENT_ACTION_LOG.get();
        return new ExecutorTask<>(task, logManager, context, parentActionLog);
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
