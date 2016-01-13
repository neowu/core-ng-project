package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author tminglei
 */
public final class WeeklyTrigger extends Trigger {
    private final DayOfWeek dayOfWeek;
    private final LocalTime time;

    public WeeklyTrigger(String name, Job job, DayOfWeek dayOfWeek, LocalTime time) {
        super(name, job);
        this.dayOfWeek = dayOfWeek;
        this.time = time;
    }

    @Override
    Duration nextDelay(LocalDateTime now) {
        LocalDateTime target = LocalDateTime.of(now.toLocalDate(), time).plusDays(dayOfWeek.getValue() - now.getDayOfWeek().getValue());

        Duration delay = Duration.between(now, target);
        if (delay.isNegative()) {
            target = target.plusWeeks(1);
            return Duration.between(now, target);
        }
        return delay;
    }

    @Override
    public String schedule() {
        return "weekly@" + dayOfWeek + "/" + time;
    }
}
