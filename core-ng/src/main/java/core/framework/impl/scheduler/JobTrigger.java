package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;
import core.framework.impl.scheduler.trigger.Trigger;

/**
 * @author neo
 */
class JobTrigger {
    final Job job;
    final Trigger trigger;

    JobTrigger(Job job, Trigger trigger) {
        this.job = job;
        this.trigger = trigger;
    }
}
