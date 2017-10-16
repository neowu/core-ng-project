package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.ZonedDateTime.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthlyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        MonthlyTrigger trigger = new MonthlyTrigger(null, null, 2, LocalTime.of(3, 0), US);   // @2T3:00 every month

        ZonedDateTime next = trigger.next(of(parse("2017-04-02T02:00:00"), US));
        assertZonedDateTimeEquals("2017-04-02T03:00:00", next);

        next = trigger.next(of(parse("2017-04-02T02:00:00"), US).withZoneSameInstant(ZoneId.of("UTC")));
        assertZonedDateTimeEquals("2017-04-02T03:00:00", next);

        next = trigger.next(of(parse("2017-04-02T03:00:00"), US));
        assertZonedDateTimeEquals("2017-05-02T03:00:00", next);

        next = trigger.next(of(parse("2017-04-02T03:30:00"), US));
        assertZonedDateTimeEquals("2017-05-02T03:00:00", next);
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(of(parse(expected), US).toInstant(), zonedDateTime.toInstant());
    }
}
