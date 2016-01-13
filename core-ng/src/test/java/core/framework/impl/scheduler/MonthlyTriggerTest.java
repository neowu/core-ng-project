package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.Assert.assertEquals;

public class MonthlyTriggerTest {
    @Test
    public void nextDelay() {
        MonthlyTrigger trigger = new MonthlyTrigger(null, null, 2, LocalTime.of(3, 0, 0));  // @2T3:00 every month

        assertEquals(Duration.ofHours(1), trigger.nextDelay(LocalDateTime.of(2016, Month.JANUARY, 2, 2, 0, 0)));
        assertEquals("wait 2 days and 1 hour to next month", Duration.ofHours(2 * 24 + 1), trigger.nextDelay(LocalDateTime.of(2016, Month.JANUARY, 31, 2, 0, 0)));
    }
}
