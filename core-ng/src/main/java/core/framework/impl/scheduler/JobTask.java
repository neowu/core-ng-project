package core.framework.impl.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public final class JobTask implements Runnable {
    private final Scheduler scheduler;
    private final Trigger trigger;

    public JobTask(Scheduler scheduler, Trigger trigger) {
        this.scheduler = scheduler;
        this.trigger = trigger;
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        Duration nextDelay = trigger.nextDelay(now);
        scheduler.schedule(this, nextDelay);
        scheduler.submitJob(trigger);
    }
}
