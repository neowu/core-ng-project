package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class WeeklyTriggerTest {

    @Test
    public void testInitialDelay() {
        int currDayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        LocalDateTime atTime = LocalDateTime.now().withHour(8);

        int beforeDay = currDayOfWeek -1;
        if (beforeDay > 0) {
            WeeklyTrigger trigger = new WeeklyTrigger("test", null, beforeDay, atTime.toLocalTime());
            Duration initialDelay = trigger.initialDelay();
            Duration expected = Duration.between(LocalDateTime.now(), atTime.minusDays(1).plusDays(7));
            assertEquals(expected, initialDelay);
        }

        int afterDay = currDayOfWeek +1;
        if (afterDay <= 7) {
            WeeklyTrigger trigger = new WeeklyTrigger("test", null, afterDay, atTime.toLocalTime());
            Duration initialDelay = trigger.initialDelay();
            Duration expected = Duration.between(LocalDateTime.now(), atTime.plusDays(1));
            assertEquals(expected, initialDelay);
        }
    }

    @Test
    public void testRate() {
        WeeklyTrigger trigger = new WeeklyTrigger("test", null, 1, LocalTime.now());
        assertEquals(Duration.ofDays(7), trigger.rate());
    }
}
