package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author tminglei
 */
public final class WeeklyTrigger implements Trigger {
    private final String name;
    private final Job job;
    private final DayOfWeek dayOfWeek;
    private final LocalTime time;

    public WeeklyTrigger(String name, Job job, DayOfWeek dayOfWeek, LocalTime time) {
        this.name = name;
        this.job = job;
        this.dayOfWeek = dayOfWeek;
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
        scheduler.schedule(this, delay, Duration.ofDays(7));
    }

    @Override
    public String frequency() {
        return "weekly@" + dayOfWeek + "/" + time;
    }

    Duration nextDelay(LocalDateTime now) {
        LocalDateTime target = LocalDateTime.of(now.toLocalDate(), time).plusDays(dayOfWeek.getValue() - now.getDayOfWeek().getValue());

        Duration delay = Duration.between(now, target);
        if (delay.isNegative()) {
            return delay.plusDays(7);
        }
        return delay;
    }
}
