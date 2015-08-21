package core.framework.impl.scheduler;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class DailyTriggerTest {
    @Test
    public void delayToNextScheduledTime() {
        DailyTrigger trigger = new DailyTrigger(null, null, null);

        Assert.assertEquals("now is 00:30, wait to 01:00", Duration.ofMinutes(30), trigger.delayToNextScheduledTime(LocalTime.of(1, 0), LocalTime.of(0, 30)));

        Assert.assertEquals("now is 23:00, wait to tomorrow 00:30", Duration.ofMinutes(90), trigger.delayToNextScheduledTime(LocalTime.of(0, 30), LocalTime.of(23, 0)));
    }
}