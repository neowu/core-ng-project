package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.LocalTime.of;
import static java.time.ZonedDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class DailyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        DailyTrigger trigger = new DailyTrigger(null, null, of(10, 0), US);

        assertZonedDateTimeEquals("2017-04-10T10:00:00", trigger.next(of(parse("2017-04-10T09:00:00"), US)));
        assertZonedDateTimeEquals("2017-04-10T10:00:00", trigger.next(of(parse("2017-04-10T09:00:00"), US).withZoneSameInstant(ZoneId.of("UTC"))));

        assertZonedDateTimeEquals("2017-04-11T10:00:00", trigger.next(of(parse("2017-04-10T10:00:00"), US)));
        assertZonedDateTimeEquals("2017-04-11T10:00:00", trigger.next(of(parse("2017-04-10T11:00:00"), US)));
    }

    @Test
    void nextWithDayLightSavingStart() {
        DailyTrigger trigger = new DailyTrigger(null, null, of(2, 30), US);

        assertZonedDateTimeEquals("2017-03-12T02:30:00", trigger.next(of(parse("2017-03-12T01:00:00"), US)));  // daylight saving started at 2017/03/12
        assertZonedDateTimeEquals("2017-03-13T02:30:00", trigger.next(of(parse("2017-03-12T02:30:00"), US)));
        assertZonedDateTimeEquals("2017-03-14T02:30:00", trigger.next(of(parse("2017-03-13T02:30:00"), US)));
    }

    @Test
    void nextWithDayLightSavingEnd() {
        DailyTrigger trigger = new DailyTrigger(null, null, of(2, 0), US);

        assertZonedDateTimeEquals("2017-11-05T02:00:00", trigger.next(of(parse("2017-11-05T00:00:00"), US)));    // daylight saving ended at 2017/11/05
        assertZonedDateTimeEquals("2017-11-06T02:00:00", trigger.next(of(parse("2017-11-05T02:00:00"), US)));
        assertZonedDateTimeEquals("2017-11-06T02:00:00", trigger.next(of(parse("2017-11-05T02:30:00"), US)));
    }

    @Test
    void frequency() {
        DailyTrigger trigger = new DailyTrigger(null, null, of(2, 30), US);

        assertEquals("daily@02:30[America/New_York]", trigger.frequency());
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(of(parse(expected), US).toInstant(), zonedDateTime.toInstant());
    }
}
