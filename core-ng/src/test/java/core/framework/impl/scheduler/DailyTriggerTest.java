package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.ZonedDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class DailyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(10, 0), US);

        ZonedDateTime next = trigger.next(of(parse("2017-04-10T09:00:00"), US));
        assertZonedDateTimeEquals("2017-04-10T10:00:00", next);

        next = trigger.next(of(parse("2017-04-10T09:00:00"), US).withZoneSameInstant(ZoneId.of("UTC")));
        assertZonedDateTimeEquals("2017-04-10T10:00:00", next);

        next = trigger.next(of(parse("2017-04-10T10:00:00"), US));
        assertZonedDateTimeEquals("2017-04-11T10:00:00", next);

        next = trigger.next(of(parse("2017-04-10T11:00:00"), US));
        assertZonedDateTimeEquals("2017-04-11T10:00:00", next);
    }

    @Test
    void nextWithDayLightSavingStart() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(2, 30), US);

        ZonedDateTime next = trigger.next(of(parse("2017-03-12T01:00:00"), US));    // daylight saving started at 2017/03/12
        assertZonedDateTimeEquals("2017-03-12T02:30:00", next);

        next = trigger.next(next);
        assertZonedDateTimeEquals("2017-03-13T02:30:00", next);

        next = trigger.next(next);
        assertZonedDateTimeEquals("2017-03-14T02:30:00", next);
    }

    @Test
    void nextWithDayLightSavingEnd() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(2, 0), US);

        ZonedDateTime next = trigger.next(of(parse("2017-11-05T00:00:00"), US));    // daylight saving ended at 2017/11/05
        assertZonedDateTimeEquals("2017-11-05T02:00:00", next);

        next = trigger.next(next);
        assertZonedDateTimeEquals("2017-11-06T02:00:00", next);
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(of(parse(expected), US).toInstant(), zonedDateTime.toInstant());
    }
}
