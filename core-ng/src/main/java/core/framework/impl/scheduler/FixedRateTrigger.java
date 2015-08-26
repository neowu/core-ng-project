package core.framework.impl.scheduler;


import core.framework.api.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;

/**
 * @author neo
 */
public class FixedRateTrigger extends Trigger {
    private static final Random RANDOM = new Random();

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

    Duration initialDelay() {
        long delay = 10000;
        double times = 1 + RANDOM.nextDouble() / 10 * 4 - 0.2; // +/-20% random
        long adjustedDelay = (long) (delay * times);
        return Duration.ofMillis(adjustedDelay);
    }

    @Override
    public String scheduleInfo() {
        return "fixed-rate@" + rate;
    }
}
