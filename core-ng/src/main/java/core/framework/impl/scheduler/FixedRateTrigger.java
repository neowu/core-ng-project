package core.framework.impl.scheduler;


import core.framework.api.scheduler.Job;
import core.framework.api.util.Randoms;

import java.time.Duration;

/**
 * @author neo
 */
public final class FixedRateTrigger implements Trigger {
    private final String name;
    private final Job job;
    private final Duration rate;

    public FixedRateTrigger(String name, Job job, Duration rate) {
        this.name = name;
        this.job = job;
        this.rate = rate;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Job job() {
        return job;
    }

    @Override
    public void schedule(Scheduler scheduler) {
        Duration delay = Duration.ofMillis((long) Randoms.number(8000, 15000)); // delay 8s to 15s
        scheduler.schedule(this, delay, rate);
    }

    @Override
    public String frequency() {
        return "fixed-rate@" + rate;
    }
}
