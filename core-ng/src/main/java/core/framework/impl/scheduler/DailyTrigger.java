package core.framework.impl.scheduler;

import core.framework.scheduler.Job;
import core.framework.util.Strings;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public final class DailyTrigger implements DynamicTrigger {
    private final String name;
    private final Job job;
    private final LocalTime time;
    private final ZoneId zoneId;

    public DailyTrigger(String name, Job job, LocalTime time, ZoneId zoneId) {
        this.name = name;
        this.job = job;
        this.time = time;
        this.zoneId = zoneId;
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
        return Strings.format("daily@{}[{}]", time, zoneId.getId());
    }

    @Override
    public ZonedDateTime next(ZonedDateTime now) {
        ZonedDateTime next = now.withZoneSameInstant(zoneId).with(time);
        if (!next.isAfter(now)) {
            next = next.plusDays(1).with(time);     // reset time in case the current day is daylight saving start date
        }
        return next;
    }
}
