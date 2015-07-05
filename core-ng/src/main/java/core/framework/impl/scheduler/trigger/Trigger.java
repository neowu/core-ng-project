package core.framework.impl.scheduler.trigger;

import core.framework.api.scheduler.Job;
import core.framework.impl.scheduler.Scheduler;

/**
 * @author neo
 */
public interface Trigger {
    void schedule(Scheduler scheduler, String name, Job job);
}
