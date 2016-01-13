package core.framework.impl.scheduler;


import core.framework.api.scheduler.Job;
import core.framework.api.util.Randoms;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public final class FixedRateTrigger extends Trigger {
    private final Duration rate;
    private boolean initialized;

    public FixedRateTrigger(String name, Job job, Duration rate) {
        super(name, job);
        this.rate = rate;
    }

    @Override
    Duration nextDelay(LocalDateTime now) {
        if (!initialized) {
            initialized = true;
            return Duration.ofMillis((long) Randoms.number(8000, 15000));   // delay 8s to 15s
        }
        return rate;
    }

    @Override
    public String schedule() {
        return "fixed-rate@" + rate;
    }
}
