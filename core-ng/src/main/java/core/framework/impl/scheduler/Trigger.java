package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public abstract class Trigger {
    public final String name;
    public final Job job;

    Trigger(String name, Job job) {
        this.name = name;
        this.job = job;
    }

    abstract Duration nextDelay(LocalDateTime now);

    public abstract String schedule();
}
