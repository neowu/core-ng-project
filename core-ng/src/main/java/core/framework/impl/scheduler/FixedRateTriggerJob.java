package core.framework.impl.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
class FixedRateTriggerJob implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(FixedRateTriggerJob.class);
    private final Scheduler scheduler;
    private final Trigger trigger;

    FixedRateTriggerJob(Scheduler scheduler, Trigger trigger) {
        this.scheduler = scheduler;
        this.trigger = trigger;
    }

    @Override
    public void run() {
        logger.info("execute scheduled job, job={}", trigger.name());
        scheduler.submitJob(trigger, false);
    }
}
