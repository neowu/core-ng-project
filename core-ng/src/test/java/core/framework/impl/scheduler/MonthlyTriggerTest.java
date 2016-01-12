package core.framework.impl.scheduler;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class MonthlyTriggerTest {

    @Test
    public void testInitialDelay() {
        int currDayOfMonth = LocalDateTime.now().getDayOfMonth();
        LocalDateTime atTime = LocalDateTime.now().withHour(8);

        int beforeDay = currDayOfMonth -1;
        if (beforeDay > 0) {
            MonthlyTrigger trigger = new MonthlyTrigger("test", null, beforeDay, atTime.toLocalTime());
            Duration initialDelay = trigger.initialDelay();
            Duration expected = Duration.between(LocalDateTime.now(), atTime.minusDays(1).plusMonths(1));
            assertEquals(expected, initialDelay);
        }

        int afterDay = currDayOfMonth +1;
        if (afterDay <= 28) {
            MonthlyTrigger trigger = new MonthlyTrigger("test", null, afterDay, atTime.toLocalTime());
            Duration initialDelay = trigger.initialDelay();
            Duration expected = Duration.between(LocalDateTime.now(), atTime.plusDays(1));
            assertEquals(expected, initialDelay);
        }
    }

    @Test
    public void testNextDelay() {
        MonthlyTrigger trigger = new MonthlyTrigger("test", null, 3, LocalTime.now());
        Duration expected = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
        assertEquals(expected, trigger.nextDelay());
    }
}
