package core.framework.impl.scheduler;

import core.framework.impl.async.ThreadPools;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.scheduler.Job;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Threads;
import core.framework.web.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public final Map<String, Trigger> triggers = Maps.newHashMap();
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService scheduler = ThreadPools.singleThreadScheduler("scheduler-");
    private final ExecutorService jobExecutor = ThreadPools.cachedThreadPool(Threads.availableProcessors() * 4, "scheduler-job-");
    private final LogManager logManager;

    public Scheduler(LogManager logManager) {
        this.logManager = logManager;
    }

    public void start() {
        triggers.forEach((name, trigger) -> {
            logger.info("schedule job, job={}, frequency={}, jobClass={}", name, trigger.frequency(), trigger.job().getClass().getCanonicalName());
            trigger.schedule(this);
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

    public void addTrigger(Trigger trigger) {
        Class<? extends Job> jobClass = trigger.job().getClass();
        if (jobClass.isSynthetic())
            throw Exceptions.error("job class must not be anonymous class or lambda, please create static class, jobClass={}", jobClass.getCanonicalName());

        String name = trigger.name();
        Trigger previous = triggers.putIfAbsent(name, trigger);
        if (previous != null)
            throw Exceptions.error("found duplicate job, name={}, previous={}", name, previous.job().getClass().getCanonicalName());
    }

    void schedule(DynamicTrigger trigger, ZonedDateTime next) {
        ZonedDateTime now = ZonedDateTime.now();
        Duration delay = Duration.between(now, next);
        scheduler.schedule(new DynamicTriggerJob(this, trigger, next), delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    void schedule(Trigger trigger, Duration delay, Duration rate) {
        scheduler.scheduleAtFixedRate(new FixedRateTriggerJob(this, trigger), delay.toNanos(), rate.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void triggerNow(String name) {
        Trigger trigger = triggers.get(name);
        if (trigger == null) throw new NotFoundException("job not found, name=" + name);
        submitJob(trigger, true);
    }

    void submitJob(Trigger trigger, boolean trace) {
        jobExecutor.submit(() -> {
            try {
                logManager.begin("=== job execution begin ===");
                String name = trigger.name();
                ActionLog actionLog = logManager.currentActionLog();
                actionLog.action("job/" + name);
                if (trace) {
                    actionLog.trace = true;
                }
                Job job = trigger.job();
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
