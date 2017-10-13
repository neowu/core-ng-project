package core.framework.impl.scheduler;

import core.framework.scheduler.Job;
import core.framework.util.Exceptions;
import core.framework.util.Strings;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author tminglei, neo
 */
public final class MonthlyTrigger implements DynamicTrigger {
    private final String name;
    private final Job job;
    private final int dayOfMonth;
    private final LocalTime time;
    private final ZoneId zoneId;

    public MonthlyTrigger(String name, Job job, int dayOfMonth, LocalTime time, ZoneId zoneId) {
        this.name = name;
        this.job = job;
        this.dayOfMonth = dayOfMonth;
        this.time = time;
        this.zoneId = zoneId;

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
        ZonedDateTime next = next(ZonedDateTime.now());
        scheduler.schedule(this, next);
    }

    @Override
    public String frequency() {
        return Strings.format("monthly@{}/{}[{}]", dayOfMonth, time, zoneId.getId());
    }

    @Override
    public ZonedDateTime next(ZonedDateTime now) {
        ZonedDateTime next = now.withZoneSameInstant(zoneId).withDayOfMonth(dayOfMonth).with(time);
        if (!next.isAfter(now)) {
            next = next.plusMonths(1).with(time);     // reset time in case the current day is daylight saving start date
        }
        return next;
    }
}
