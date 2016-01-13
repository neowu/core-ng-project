package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class DailyTriggerTest {
    @Test
    public void nextDelay() {
        DailyTrigger trigger = new DailyTrigger(null, null, LocalTime.of(1, 0)); // 1:00 every day

        assertEquals(Duration.ofMinutes(30), trigger.nextDelay(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 30, 0))));
        assertEquals("wait 90 mins to next day", Duration.ofMinutes(90), trigger.nextDelay(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 30, 0))));
    }
}