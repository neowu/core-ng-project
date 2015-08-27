package core.framework.impl.scheduler;


import core.framework.api.scheduler.Job;
import core.framework.api.util.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class FixedRateTrigger extends Trigger {
    private final Logger logger = LoggerFactory.getLogger(FixedRateTrigger.class);

    final Duration rate;

    public FixedRateTrigger(String name, Job job, Duration rate) {
        super(name, job);
        this.rate = rate;
    }

    @Override
    void schedule(Scheduler scheduler) {
        logger.info("scheduled fixed rate job, name={}, rate={}, job={}", name, rate, job.getClass().getCanonicalName());
        scheduler.schedule(name, job, initialDelay(), rate);
    }

    private Duration initialDelay() {
        return Duration.ofMillis((long) Randoms.number(8000, 15000));   // delay 8s to 15s
    }

    @Override
    public String scheduleInfo() {
        return "fixed-rate@" + rate;
    }
}
