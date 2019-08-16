package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author keith
 */
class HourlyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        HourlyTrigger trigger = new HourlyTrigger(0, 0);
        assertThat(trigger.next(date("2019-08-16T09:00:00"))).isEqualTo(date("2019-08-16T10:00:00"));

        trigger = new HourlyTrigger(30, 0);
        assertZonedDateTimeEquals("2019-08-16T10:30:00", trigger.next(date("2019-08-16T09:30:00")));

        trigger = new HourlyTrigger(0, 30);
        assertZonedDateTimeEquals("2019-08-16T10:00:30", trigger.next(date("2019-08-16T09:00:30")));
    }

    @Test
    void description() {
        HourlyTrigger trigger = new HourlyTrigger(2, 30);

        assertEquals("hourly@2:30", trigger.toString());
    }

    private ZonedDateTime date(String date) {
        return ZonedDateTime.of(parse(date), US);
    }

    private void assertZonedDateTimeEquals(String expected, ZonedDateTime zonedDateTime) {
        assertEquals(date(expected).toInstant(), zonedDateTime.toInstant());
    }
}
