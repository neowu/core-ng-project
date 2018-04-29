package core.framework.impl.scheduler;

import core.framework.scheduler.Trigger;
import core.framework.util.Strings;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * @author tminglei, neo
 */
public final class WeeklyTrigger implements Trigger {
    private final DayOfWeek dayOfWeek;
    private final LocalTime time;

    public WeeklyTrigger(DayOfWeek dayOfWeek, LocalTime time) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
    }

    @Override
    public ZonedDateTime next(ZonedDateTime previous) {
        ZonedDateTime next = previous.plusDays(dayOfWeek.getValue() - previous.getDayOfWeek().getValue()).with(time);
        if (!next.isAfter(previous)) {
            next = next.plusWeeks(1).with(time);     // reset time in case the current day is daylight saving start date
        }
        return next;
    }

    @Override
    public String toString() {
        return Strings.format("weekly@{}/{}", dayOfWeek, time);
    }
}
