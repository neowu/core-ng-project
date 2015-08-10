package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.NotFoundException;
import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.scheduler.trigger.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class Scheduler {
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Executor executor;
    private final LogManager logManager;
    private final Map<String, JobTrigger> triggers = Maps.newHashMap();

    public Scheduler(Executor executor, LogManager logManager) {
        this.executor = executor;
        this.logManager = logManager;
    }

    public void start() {
        triggers.forEach((name, trigger) -> trigger.trigger.schedule(this, name, trigger.job));
        logger.info("scheduler started");
    }

    public void shutdown() {
        logger.info("shutdown scheduler");
        scheduler.shutdown();
    }

    public void addTrigger(String name, Job job, Trigger trigger) {
        if (job.getClass().isSynthetic())
            throw Exceptions.error("job class must not be anonymous class or lambda, please create static class, jobClass={}", job.getClass().getCanonicalName());

        JobTrigger previous = triggers.putIfAbsent(name, new JobTrigger(job, trigger));
        if (previous != null)
            throw Exceptions.error("duplicated job found, name={}, previousJobClass={}", name, previous.job.getClass().getCanonicalName());
    }

    public void triggerNow(String name) {
        JobTrigger trigger = triggers.get(name);
        if (trigger == null) throw new NotFoundException("job name not found, name=" + name);
        Job job = trigger.job;
        executor.submit(task(name, job, true));
    }

    public void schedule(String name, Job job, Duration initialDelay, Duration rate) {
        scheduler.scheduleAtFixedRate(() -> executor.submit(task(name, job, false)), initialDelay.getSeconds(), rate.getSeconds(), TimeUnit.SECONDS);
    }

    private Callable<Void> task(String name, Job job, boolean trace) {
        return () -> {
            logger.info("execute scheduled job, name={}", name);
            ActionLog actionLog = logManager.currentActionLog();
            actionLog.action("job/" + name);
            actionLog.putContext("job", job.getClass().getCanonicalName());
            if (trace) {
                actionLog.triggerTraceLog();
            }
            job.execute();
            return null;
        };
    }

    static class JobTrigger {
        final Job job;
        final Trigger trigger;

        JobTrigger(Job job, Trigger trigger) {
            this.job = job;
            this.trigger = trigger;
        }
    }
}
