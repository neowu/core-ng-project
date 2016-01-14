package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author neo
 */
public final class DailyTrigger implements Trigger {
    private final String name;
    private final Job job;
    private final LocalTime time;

    public DailyTrigger(String name, Job job, LocalTime time) {
        this.name = name;
        this.job = job;
        this.time = time;
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
        Duration delay = nextDelay(LocalDateTime.now());
        scheduler.schedule(this, delay, Duration.ofDays(1));
    }

    @Override
    public String frequency() {
        return "daily@" + time;
    }

    Duration nextDelay(LocalDateTime now) {
        Duration delay = Duration.between(now.toLocalTime(), time);
        if (delay.isNegative()) {
            return delay.plusDays(1);
        }
        return delay;
    }
}
