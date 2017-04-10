package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.ZonedDateTime.of;
import static org.junit.Assert.assertEquals;

public class MonthlyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    public void next() {
        MonthlyTrigger trigger = new MonthlyTrigger(null, null, 2, LocalTime.of(3, 0), US);   // @2T3:00 every month

        ZonedDateTime next = trigger.next(of(parse("2017-04-02T02:00:00"), US));
        assertEquals("next should be 2017-04-02T03:00:00", of(parse("2017-04-02T03:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2017-04-02T02:00:00"), US).withZoneSameInstant(ZoneId.of("UTC")));
        assertEquals("next should be 2017-04-02T03:00:00", of(parse("2017-04-02T03:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2017-04-02T03:00:00"), US));
        assertEquals("next should be 2017-05-02T03:00:00", of(parse("2017-05-02T03:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2017-04-02T03:30:00"), US));
        assertEquals("next should be 2017-05-02T03:00:00", of(parse("2017-05-02T03:00:00"), US).toInstant(), next.toInstant());
    }
}
