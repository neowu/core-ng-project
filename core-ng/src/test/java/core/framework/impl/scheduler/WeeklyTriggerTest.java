package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.LocalTime.of;
import static java.time.ZonedDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeeklyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/Los_Angeles");

    @Test
    void next() {
        WeeklyTrigger trigger = new WeeklyTrigger(DayOfWeek.WEDNESDAY, of(2, 0));  // @WednesdayT2:00 every week

        assertZonedDateTimeEquals("2016-01-13T02:00:00", trigger.next(date("2016-01-13T01:00:00")));   // 2016-1-13 is Wednesday
        assertThat(trigger.next(ZonedDateTime.parse("2016-01-13T01:00:00Z"))).isEqualTo("2016-01-13T02:00:00Z");

        assertZonedDateTimeEquals("2016-01-20T02:00:00", trigger.next(date("2016-01-13T02:00:00")));
        assertZonedDateTimeEquals("2016-01-20T02:00:00", trigger.next(date("2016-01-13T02:30:00")));

        assertZonedDateTimeEquals("2016-01-13T02:00:00", trigger.next(date("2016-01-12T01:00:00")));   // 2016-1-12 is Tuesday, next should be Wednesday this week
        assertZonedDateTimeEquals("2016-01-20T02:00:00", trigger.next(date("2016-01-19T01:00:00")));   // 2016-1-19 is Thursday, next should be Wednesday next week
    }

    @Test
    void description() {
        WeeklyTrigger trigger = new WeeklyTrigger(DayOfWeek.WEDNESDAY, of(2, 0));  // @WednesdayT2:00 every week
        assertEquals("weekly@WEDNESDAY/02:00", trigger.toString());
    }

    private ZonedDateTime date(String date) {
        return of(parse(date), US);
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(date(expected).toInstant(), zonedDateTime.toInstant());
    }
}
