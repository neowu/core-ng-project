package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.ZonedDateTime.of;
import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class DailyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    public void next() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(10, 0), US);

        ZonedDateTime next = trigger.next(of(parse("2017-04-10T09:00:00"), US));
        assertEquals("next should be 2017-04-10T10:00:00", of(parse("2017-04-10T10:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2017-04-10T09:00:00"), US).withZoneSameInstant(ZoneId.of("UTC")));
        assertEquals("next should be 2017-04-10T10:00:00", of(parse("2017-04-10T10:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2017-04-10T10:00:00"), US));
        assertEquals("next should be 2017-04-11T10:00:00", of(parse("2017-04-11T10:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(of(parse("2017-04-10T11:00:00"), US));
        assertEquals("next should be 2017-04-11T10:00:00", of(parse("2017-04-11T10:00:00"), US).toInstant(), next.toInstant());
    }

    @Test
    public void nextWithDayLightSavingStart() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(2, 30), US);

        ZonedDateTime next = trigger.next(of(parse("2017-03-12T01:00:00"), US));    // daylight saving started at 2017/03/12
        assertEquals("next should be 2017-03-12T02:30:00", of(parse("2017-03-12T02:30:00"), US).toInstant(), next.toInstant());

        next = trigger.next(next);
        assertEquals("next should be 2017-03-13T02:30:00", of(parse("2017-03-13T02:30:00"), US).toInstant(), next.toInstant());

        next = trigger.next(next);
        assertEquals("next should be 2017-03-14T02:30:00", of(parse("2017-03-14T02:30:00"), US).toInstant(), next.toInstant());
    }

    @Test
    public void nextWithDayLightSavingEnd() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(2, 0), US);

        ZonedDateTime next = trigger.next(of(parse("2017-11-05T00:00:00"), US));    // daylight saving ended at 2017/11/05
        assertEquals("next should be 2017-11-05T02:00:00", of(parse("2017-11-05T02:00:00"), US).toInstant(), next.toInstant());

        next = trigger.next(next);
        assertEquals("next should be 2017-11-06T02:00:00", of(parse("2017-11-06T02:00:00"), US).toInstant(), next.toInstant());
    }
}