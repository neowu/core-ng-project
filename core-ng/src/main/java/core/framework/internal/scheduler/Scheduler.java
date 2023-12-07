package core.framework.internal.scheduler;

import core.framework.internal.async.ThreadPools;
import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.scheduler.Trigger;
import core.framework.util.Maps;
import core.framework.util.Randoms;
import core.framework.web.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static core.framework.log.Markers.errorCode;
import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class Scheduler {
    public final Map<String, Task> tasks = Maps.newLinkedHashMap();     // to log scheduling job in order at startup
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService scheduler;
    private final ExecutorService jobExecutor;
    private final LogManager logManager;
    private final long maxProcessTimeInNano = Duration.ofSeconds(10).toNanos(); // scheduler job should be fast, it may block other jobs, generally just sending kafka message
    public Clock clock = Clock.systemUTC();

    public Scheduler(LogManager logManager) {
        this(logManager, ThreadPools.singleThreadScheduler("scheduler-"), ThreadPools.virtualThreadExecutor("scheduler-job-"));
    }

    Scheduler(LogManager logManager, ScheduledExecutorService scheduler, ExecutorService jobExecutor) {
        this.logManager = logManager;
        this.scheduler = scheduler;
        this.jobExecutor = jobExecutor;
    }

    public void start() {
        var now = ZonedDateTime.now(clock);
        for (var entry : tasks.entrySet()) {
            String name = entry.getKey();
            Task task = entry.getValue();
            if (task instanceof final FixedRateTask fixedRateTask) {
                schedule(fixedRateTask);
                logger.info("schedule job, job={}, trigger={}, jobClass={}", name, task.trigger(), task.job().getClass().getCanonicalName());
            } else if (task instanceof TriggerTask triggerTask) {
                try {
                    ZonedDateTime next = next(triggerTask.trigger, now);
                    schedule(triggerTask, next);
                    logger.info("schedule job, job={}, trigger={}, jobClass={}, next={}", name, task.trigger(), task.job().getClass().getCanonicalName(), next);
                } catch (Throwable e) {
                    logger.error("failed to schedule job, job={}", name, e);  // next() with custom trigger impl may throw exception, we don't let runtime error fail startup
                }
            }
        }
        logger.info("scheduler started");
    }

    public void shutdown() throws InterruptedException {
        logger.info("shutting down scheduler");
        scheduler.shutdown();
        try {
            boolean success = scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);
            if (!success) logger.warn("failed to terminate scheduler");
        } finally {
            jobExecutor.shutdown();
        }
    }

    public void awaitTermination(long timeoutInMs) throws InterruptedException {
        boolean success = jobExecutor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
        if (!success) logger.warn(errorCode("FAILED_TO_STOP"), "failed to terminate scheduler job executor");
        else logger.info("scheduler stopped");
    }

    public void addFixedRateTask(String name, Job job, Duration rate) {
        addTask(new FixedRateTask(name, job, rate));
    }

    public void addTriggerTask(String name, Job job, Trigger trigger) {
        addTask(new TriggerTask(name, job, trigger, clock.getZone()));
    }

    private void addTask(Task task) {
        String name = task.name();
        Task previous = tasks.putIfAbsent(name, task);
        if (previous != null)
            throw new Error(format("found duplicate job, name={}, previous={}", name, previous.job().getClass().getCanonicalName()));
    }

    ZonedDateTime next(Trigger trigger, ZonedDateTime previous) {
        ZonedDateTime next = trigger.next(previous);
        if (next == null || !next.isAfter(previous)) throw new Error(format("next scheduled time must be after previous, previous={}, next={}", previous, next));
        return next;
    }

    void schedule(TriggerTask task, ZonedDateTime time) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        Duration delay = Duration.between(now, time);
        scheduler.schedule(() -> executeTask(task, time), delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    void schedule(FixedRateTask task) {
        Duration delay = Duration.ofMillis((long) Randoms.nextDouble(1000, 3000)); // delay 1s to 3s to shuffle fix rated jobs
        task.scheduledTime = ZonedDateTime.now(clock).plus(delay);
        scheduler.scheduleAtFixedRate(() -> {
            ZonedDateTime scheduledTime = task.scheduledTime;
            ZonedDateTime next = task.scheduleNext();
            logger.info("execute scheduled job, job={}, rate={}, scheduled={}, next={}", task.name(), task.rate, scheduledTime, next);
            submitJob(task, scheduledTime, null);
        }, delay.toNanos(), task.rate.toNanos(), TimeUnit.NANOSECONDS);
    }

    void executeTask(TriggerTask task, ZonedDateTime scheduledTime) {
        try {
            ZonedDateTime next = next(task.trigger, scheduledTime);
            schedule(task, next);
            logger.info("execute scheduled job, job={}, trigger={}, scheduled={}, next={}", task.name(), task.trigger(), scheduledTime, next);
            submitJob(task, scheduledTime, null);
        } catch (Throwable e) {
            logger.error("failed to execute scheduled job, job is terminated, job={}, error={}", task.name(), e.getMessage(), e);
        }
    }

    public void triggerNow(String name, String triggerActionId) {
        Task task = tasks.get(name);
        if (task == null) throw new NotFoundException("job not found, name=" + name);
        submitJob(task, ZonedDateTime.now(clock), triggerActionId);
    }

    private void submitJob(Task task, ZonedDateTime scheduledTime, @Nullable String triggerActionId) {
        jobExecutor.submit(() -> {
            VirtualThread.COUNT.increase();
            ActionLog actionLog = logManager.begin("=== job execution begin ===", null);
            try {
                String name = task.name();
                actionLog.action("job:" + name);
                if (triggerActionId != null) {  // triggered by scheduler controller
                    actionLog.refIds = List.of(triggerActionId);
                    actionLog.correlationIds = List.of(triggerActionId);
                    actionLog.trace = Trace.CASCADE;
                }
                actionLog.warningContext.maxProcessTimeInNano(maxProcessTimeInNano);
                actionLog.context("trigger", task.trigger());
                Job job = task.job();
                actionLog.context("job", name);
                actionLog.context("job_class", job.getClass().getCanonicalName());
                actionLog.context("scheduled_time", scheduledTime.toInstant());
                job.execute(new JobContext(name, scheduledTime));
                return null;
            } catch (Throwable e) {
                logManager.logError(e);
                throw e;
            } finally {
                logManager.end("=== job execution end ===");
                VirtualThread.COUNT.decrease();
            }
        });
    }
}
