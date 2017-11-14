package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.LocalTime.of;
import static java.time.ZonedDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeeklyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/Los_Angeles");

    @Test
    void next() {
        WeeklyTrigger trigger = new WeeklyTrigger(null, null, DayOfWeek.WEDNESDAY, of(2, 0, 0), US);  // @WednesdayT2:00 every week

        assertZonedDateTimeEquals("2016-01-13T02:00:00", trigger.next(of(parse("2016-01-13T01:00:00"), US)));   // 2016-1-13 is Wednesday
        assertZonedDateTimeEquals("2016-01-13T02:00:00", trigger.next(of(parse("2016-01-13T01:00:00"), US).withZoneSameInstant(ZoneId.of("UTC"))));

        assertZonedDateTimeEquals("2016-01-20T02:00:00", trigger.next(of(parse("2016-01-13T02:00:00"), US)));
        assertZonedDateTimeEquals("2016-01-20T02:00:00", trigger.next(of(parse("2016-01-13T02:30:00"), US)));

        assertZonedDateTimeEquals("2016-01-13T02:00:00", trigger.next(of(parse("2016-01-12T01:00:00"), US)));   // 2016-1-12 is Tuesday, next should be Wednesday this week
        assertZonedDateTimeEquals("2016-01-20T02:00:00", trigger.next(of(parse("2016-01-19T01:00:00"), US)));   // 2016-1-19 is Thursday, next should be Wednesday next week
    }

    @Test
    void frequency() {
        WeeklyTrigger trigger = new WeeklyTrigger(null, null, DayOfWeek.WEDNESDAY, of(2, 0, 0), US);  // @WednesdayT2:00 every week
        assertEquals("weekly@WEDNESDAY/02:00[America/Los_Angeles]", trigger.frequency());

        trigger = new WeeklyTrigger(null, null, DayOfWeek.MONDAY, of(12, 0, 0), ZoneId.of("UTC"));  // @WednesdayT2:00 every week
        assertEquals("weekly@MONDAY/12:00[UTC]", trigger.frequency());
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(of(parse(expected), US).toInstant(), zonedDateTime.toInstant());
    }
}
