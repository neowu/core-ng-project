package core.framework.internal.scheduler;

import core.framework.scheduler.Trigger;

import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public final class DailyTrigger implements Trigger {
    private final LocalTime time;

    public DailyTrigger(LocalTime time) {
        this.time = time;
    }

    @Override
    public ZonedDateTime next(ZonedDateTime previous) {
        ZonedDateTime next = previous.with(time);
        if (!next.isAfter(previous)) {
            next = next.plusDays(1).with(time);     // reset time in case the current day is daylight saving start date
        }
        return next;
    }

    @Override
    public String toString() {
        return "daily@" + time;
    }
}
