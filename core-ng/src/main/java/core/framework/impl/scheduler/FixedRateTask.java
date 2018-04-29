package core.framework.impl.scheduler;


import core.framework.scheduler.Job;

import java.time.Duration;

/**
 * @author neo
 */
public final class FixedRateTask implements Task {
    final Duration rate;
    private final String name;
    private final Job job;

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
}
