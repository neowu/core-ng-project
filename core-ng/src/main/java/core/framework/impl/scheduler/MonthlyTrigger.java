package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;
import core.framework.api.util.Exceptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author tminglei
 */
public final class MonthlyTrigger implements DynamicTrigger {
    private final String name;
    private final Job job;
    private final int dayOfMonth;
    private final LocalTime time;

    public MonthlyTrigger(String name, Job job, int dayOfMonth, LocalTime time) {
        this.name = name;
        this.job = job;
        this.dayOfMonth = dayOfMonth;
        this.time = time;

        if (dayOfMonth < 1 || dayOfMonth > 28) {
            throw Exceptions.error("unsupported dayOfMonth, please use 1-28, dayOfMonth={}", dayOfMonth);
        }
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
        scheduler.schedule(this, delay);
    }

    @Override
    public String frequency() {
        return "monthly@" + dayOfMonth + "/" + time;
    }

    @Override
    public Duration nextDelay(LocalDateTime now) {
        LocalDateTime target = LocalDateTime.of(now.toLocalDate(), time).withDayOfMonth(dayOfMonth);
        Duration delay = Duration.between(now, target);
        if (delay.isZero() || delay.isNegative()) { // make sure delay is positive
            target = target.plusMonths(1);
            return Duration.between(now, target);
        }
        return delay;
    }
}
