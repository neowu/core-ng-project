package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author neo
 */
public final class DailyTrigger extends Trigger {
    private final LocalTime time;

    public DailyTrigger(String name, Job job, LocalTime time) {
        super(name, job);
        this.time = time;
    }

    @Override
    Duration nextDelay(LocalDateTime now) {
        Duration delay = Duration.between(now.toLocalTime(), time);
        if (delay.isNegative()) {
            return delay.plus(Duration.ofDays(1));
        }
        return delay;
    }

    @Override
    public String schedule() {
        return "daily@" + time;
    }
}
