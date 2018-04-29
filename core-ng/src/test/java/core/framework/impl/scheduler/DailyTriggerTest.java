package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.LocalTime.of;
import static java.time.ZonedDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class DailyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        DailyTrigger trigger = new DailyTrigger(of(10, 0));

        assertZonedDateTimeEquals("2017-04-10T10:00:00", trigger.next(date("2017-04-10T09:00:00")));
        assertThat(trigger.next(ZonedDateTime.parse("2017-04-10T09:00:00Z"))).isEqualTo("2017-04-10T10:00:00Z");

        assertZonedDateTimeEquals("2017-04-11T10:00:00", trigger.next(date("2017-04-10T10:00:00")));
        assertZonedDateTimeEquals("2017-04-11T10:00:00", trigger.next(date("2017-04-10T11:00:00")));
    }

    @Test
    void nextWithDayLightSavingStart() {
        DailyTrigger trigger = new DailyTrigger(of(2, 30));

        assertZonedDateTimeEquals("2017-03-12T02:30:00", trigger.next(date("2017-03-12T01:00:00")));  // daylight saving started at 2017/03/12
        assertZonedDateTimeEquals("2017-03-13T02:30:00", trigger.next(date("2017-03-12T02:30:00")));
        assertZonedDateTimeEquals("2017-03-14T02:30:00", trigger.next(date("2017-03-13T02:30:00")));
    }

    @Test
    void nextWithDayLightSavingEnd() {
        DailyTrigger trigger = new DailyTrigger(of(2, 0));

        assertZonedDateTimeEquals("2017-11-05T02:00:00", trigger.next(date("2017-11-05T00:00:00")));    // daylight saving ended at 2017/11/05
        assertZonedDateTimeEquals("2017-11-06T02:00:00", trigger.next(date("2017-11-05T02:00:00")));
        assertZonedDateTimeEquals("2017-11-06T02:00:00", trigger.next(date("2017-11-05T02:30:00")));
    }

    @Test
    void description() {
        DailyTrigger trigger = new DailyTrigger(of(2, 30));

        assertEquals("daily@02:30", trigger.toString());
    }

    private ZonedDateTime date(String date) {
        return of(parse(date), US);
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(date(expected).toInstant(), zonedDateTime.toInstant());
    }
}
