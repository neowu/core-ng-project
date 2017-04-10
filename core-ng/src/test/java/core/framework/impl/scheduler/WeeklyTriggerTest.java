package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.ZonedDateTime.of;
import static org.junit.Assert.assertEquals;

public class WeeklyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/Los_Angeles");

    @Test
    public void next() {
        WeeklyTrigger trigger = new WeeklyTrigger(null, null, DayOfWeek.WEDNESDAY, LocalTime.of(2, 0, 0), US);  // @MondayT2:00 every week

        ZonedDateTime next = trigger.next(of(parse("2016-01-13T01:00:00"), US));            // 2016-1-13 is Wednesday
        assertEquals("next should be 2016-01-13T02:00:00", of(parse("2016-01-13T02:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2016-01-13T01:00:00"), US).withZoneSameInstant(ZoneId.of("UTC")));
        assertEquals("next should be 2016-01-13T02:00:00", of(parse("2016-01-13T02:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2016-01-13T02:00:00"), US));
        assertEquals("next should be 2016-01-20T02:00:00", of(parse("2016-01-20T02:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2016-01-13T02:30:00"), US));
        assertEquals("next should be 2016-01-20T02:00:00", of(parse("2016-01-20T02:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2016-01-12T01:00:00"), US));            // 2016-1-12 is Tuesday
        assertEquals("next should be 2016-01-13T02:00:00", of(parse("2016-01-13T02:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2016-01-19T01:00:00"), US));            // 2016-1-19 is Thursday
        assertEquals("next should be 2016-01-20T02:00:00", of(parse("2016-01-20T02:00:00"), US).toInstant(), next.toInstant());
    }
}
