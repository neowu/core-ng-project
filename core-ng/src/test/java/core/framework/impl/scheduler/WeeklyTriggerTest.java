package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class WeeklyTriggerTest {
    @Test
    public void nextDelay() {
        WeeklyTrigger trigger = new WeeklyTrigger(null, null, DayOfWeek.WEDNESDAY, LocalTime.of(2, 0, 0));  // @MondayT2:00 every week

        assertEquals(Duration.ofHours(1), trigger.nextDelay(LocalDateTime.of(2016, Month.JANUARY, 13, 1, 0, 0)));   // 2016-1-13 is Wednesday
        assertEquals("wait 7 days and -1 hour to next week", Duration.ofHours(7 * 24 - 1), trigger.nextDelay(LocalDateTime.of(2016, Month.JANUARY, 13, 3, 0, 0)));
    }
}
