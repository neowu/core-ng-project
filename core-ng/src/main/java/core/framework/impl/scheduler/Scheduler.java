package core.framework.impl.scheduler;

import core.framework.impl.async.ThreadPools;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.scheduler.Job;
import core.framework.scheduler.Trigger;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Randoms;
import core.framework.web.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class Scheduler {
    public final Map<String, Task> tasks = Maps.newHashMap();
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService scheduler;
    private final ExecutorService jobExecutor;
    private final LogManager logManager;
    public Clock clock = Clock.systemDefaultZone();

    public Scheduler(LogManager logManager) {
        this(logManager, ThreadPools.singleThreadScheduler("scheduler-"),
                ThreadPools.cachedThreadPool(Runtime.getRuntime().availableProcessors() * 4, "scheduler-job-"));
    }

    Scheduler(LogManager logManager, ScheduledExecutorService scheduler, ExecutorService jobExecutor) {
        this.logManager = logManager;
        this.scheduler = scheduler;
        this.jobExecutor = jobExecutor;
    }

    public void start() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        tasks.forEach((name, task) -> {
            if (task instanceof FixedRateTask) {
                schedule((FixedRateTask) task);
                logger.info("schedule job, job={}, trigger={}, jobClass={}", name, task.trigger(), task.job().getClass().getCanonicalName());
            } else if (task instanceof TriggerTask) {
                try {
                    ZonedDateTime next = next(((TriggerTask) task).trigger, now);
                    schedule((TriggerTask) task, next);
                    logger.info("schedule job, job={}, trigger={}, jobClass={}, next={}", name, task.trigger(), task.job().getClass().getCanonicalName(), next);
                } catch (Throwable e) {
                    logger.error("failed to schedule job, job={}", name, e);  // next() with custom trigger impl may throw exception, we don't let runtime error fail startup
                }
            }
        });
        logger.info("scheduler started");
    }

    public void stop() {
        logger.info("stop scheduler");
        scheduler.shutdown();
        jobExecutor.shutdown();
        try {
            jobExecutor.awaitTermination(10, TimeUnit.SECONDS);     // wait 10 seconds to finish current tasks
        } catch (InterruptedException e) {
            logger.warn("failed to wait all tasks to finish", e);
        }
    }

    public void addFixedRateTask(String name, Job job, Duration rate) {
        addTask(new FixedRateTask(name, job, rate));
    }

    public void addTriggerTask(String name, Job job, Trigger trigger) {
        addTask(new TriggerTask(name, job, trigger, clock.getZone()));
    }

    private void addTask(Task task) {
        Class<? extends Job> jobClass = task.job().getClass();
        if (jobClass.isSynthetic())
            throw Exceptions.error("job class must not be anonymous class or lambda, please create static class, jobClass={}", jobClass.getCanonicalName());

        String name = task.name();
        Task previous = tasks.putIfAbsent(name, task);
        if (previous != null)
            throw Exceptions.error("found duplicate job, name={}, previous={}", name, previous.job().getClass().getCanonicalName());
    }

    ZonedDateTime next(Trigger trigger, ZonedDateTime previous) {
        ZonedDateTime next = trigger.next(previous);
        if (next == null || !next.isAfter(previous)) throw Exceptions.error("next scheduled time must be after previous, previous={}, next={}", previous, next);
        return next;
    }

    void schedule(TriggerTask task, ZonedDateTime time) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        Duration delay = Duration.between(now, time);
        scheduler.schedule(() -> executeTask(task, time), delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    void schedule(FixedRateTask task) {
        Duration delay = Duration.ofMillis((long) Randoms.number(1000, 3000)); // delay 1s to 3s
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("execute scheduled job, job={}", task.name());
            submitJob(task, false);
        }, delay.toNanos(), task.rate.toNanos(), TimeUnit.NANOSECONDS);
    }

    void executeTask(TriggerTask task, ZonedDateTime time) {
        try {
            ZonedDateTime next = next(task.trigger, time);
            schedule(task, next);
            logger.info("execute scheduled job, job={}, time={}, next={}", task.name(), time, next);
            submitJob(task, false);
        } catch (Throwable e) {
            logger.error("failed to execute scheduled job, job is terminated, job={}, error={}", task.name(), e.getMessage(), e);
        }
    }

    public void triggerNow(String name) {
        Task task = tasks.get(name);
        if (task == null) throw new NotFoundException("job not found, name=" + name);
        submitJob(task, true);
    }

    private void submitJob(Task task, boolean trace) {
        jobExecutor.submit(() -> {
            try {
                ActionLog actionLog = logManager.begin("=== job execution begin ===");
                String name = task.name();
                actionLog.action("job:" + name);
                actionLog.trace = trace;
                actionLog.context("trigger", task.trigger());
                Job job = task.job();
                actionLog.context("job", name);
                actionLog.context("jobClass", job.getClass().getCanonicalName());
                job.execute();
                return null;
            } catch (Throwable e) {
                logManager.logError(e);
                throw e;
            } finally {
                logManager.end("=== job execution end ===");
            }
        });
    }
}
