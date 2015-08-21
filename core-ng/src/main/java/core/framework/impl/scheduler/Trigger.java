package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

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

    abstract void schedule(Scheduler scheduler);

    public abstract String scheduleInfo();
}
