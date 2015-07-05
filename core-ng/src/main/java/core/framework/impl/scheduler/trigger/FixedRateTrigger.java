package core.framework.impl.scheduler.trigger;


import core.framework.api.scheduler.Job;
import core.framework.impl.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class FixedRateTrigger implements Trigger {
    private final Logger logger = LoggerFactory.getLogger(FixedRateTrigger.class);

    final Duration rate;

    public FixedRateTrigger(Duration rate) {
        this.rate = rate;
    }

    @Override
    public void schedule(Scheduler scheduler, String name, Job job) {
        logger.info("scheduled fixed rate job, name={}, rateInSeconds={}, job={}", name, rate.getSeconds(), job.getClass().getCanonicalName());
        scheduler.schedule(name, job, Duration.ofSeconds(10), rate);
    }
}
