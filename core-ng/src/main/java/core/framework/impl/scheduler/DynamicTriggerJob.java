package core.framework.impl.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
class DynamicTriggerJob implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DynamicTriggerJob.class);

    private final Scheduler scheduler;
    private final DynamicTrigger trigger;
    private final ZonedDateTime now;

    DynamicTriggerJob(Scheduler scheduler, DynamicTrigger trigger, ZonedDateTime now) {
        this.scheduler = scheduler;
        this.trigger = trigger;
        this.now = now;
    }

    @Override
    public void run() {
        ZonedDateTime next = trigger.next(now);
        scheduler.schedule(trigger, next);
        logger.info("execute scheduled job, job={}, now={}, next={}", trigger.name(), now, next);
        scheduler.submitJob(trigger, false);
    }
}
