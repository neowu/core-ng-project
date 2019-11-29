package core.framework.internal.scheduler;


import core.framework.scheduler.Job;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public final class FixedRateTask implements Task {
    final Duration rate;
    private final String name;
    private final Job job;
    ZonedDateTime scheduledTime;

    FixedRateTask(String name, Job job, Duration rate) {
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
    public String trigger() {
        return "fixedRate@" + rate;
    }

    ZonedDateTime scheduleNext() {  // only for tracking scheduled time of JobContext, the actual scheduling is managed java scheduler
        scheduledTime = scheduledTime.plus(rate);
        return scheduledTime;
    }
}
