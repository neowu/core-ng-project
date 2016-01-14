package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

/**
 * @author neo
 */
public interface Trigger {
    String name();

    Job job();

    String frequency();

    void schedule(Scheduler scheduler);
}
