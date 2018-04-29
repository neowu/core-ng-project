package core.framework.impl.scheduler;

import core.framework.scheduler.Trigger;
import core.framework.util.Exceptions;
import core.framework.util.Strings;

import java.time.LocalTime;
import java.time.ZonedDateTime;

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
            throw Exceptions.error("unsupported dayOfMonth, please use 1-28, dayOfMonth={}", dayOfMonth);
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
        return Strings.format("monthly@{}/{}", dayOfMonth, time);
    }
}
