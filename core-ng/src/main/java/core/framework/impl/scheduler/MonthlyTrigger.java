package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;
import core.framework.api.util.Exceptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author tminglei
 */
public final class MonthlyTrigger extends Trigger {
    private final int dayOfMonth;
    private final LocalTime time;

    public MonthlyTrigger(String name, Job job, int dayOfMonth, LocalTime time) {
        super(name, job);
        this.dayOfMonth = dayOfMonth;
        this.time = time;

        if (dayOfMonth < 1 || dayOfMonth > 28) {
            throw Exceptions.error("unsupported dayOfMonth, please use 1-28, dayOfMonth={}", dayOfMonth);
        }
    }

    @Override
    Duration nextDelay(LocalDateTime now) {
        LocalDateTime target = LocalDateTime.of(now.toLocalDate(), time).withDayOfMonth(dayOfMonth);
        Duration delay = Duration.between(now, target);
        if (delay.isNegative()) {
            target = target.plusMonths(1);
            return Duration.between(now, target);
        }
        return delay;
    }

    @Override
    public String schedule() {
        return "monthly@" + dayOfMonth + "/" + time;
    }
}
