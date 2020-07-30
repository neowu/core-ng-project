package core.framework.internal.scheduler;

import core.framework.scheduler.Trigger;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import static core.framework.util.Strings.format;

/**
 * @author tminglei, neo
 */
public final class MonthlyTrigger implements Trigger {
    private final int dayOfMonth;
    private final LocalTime time;

    public MonthlyTrigger(int dayOfMonth, LocalTime time) {
        this.dayOfMonth = dayOfMonth;
        this.time = time;

        if (dayOfMonth < 1 || dayOfMonth > 28) {
            throw new Error("dayOfMonth is out of range, please use 1-28, dayOfMonth=" + dayOfMonth);
        }
    }

    @Override
    public ZonedDateTime next(ZonedDateTime previous) {
        ZonedDateTime next = previous.withDayOfMonth(dayOfMonth).with(time);
        if (!next.isAfter(previous)) {
            next = next.plusMonths(1).with(time);     // reset time in case the current day is daylight saving start date
        }
        return next;
    }

    @Override
    public String toString() {
        return format("monthly@{}/{}", dayOfMonth, time);
    }
}
