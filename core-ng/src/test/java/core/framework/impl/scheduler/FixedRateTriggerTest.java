package core.framework.impl.scheduler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

/**
 * @author neo
 */
public class FixedRateTriggerTest {
    FixedRateTrigger trigger;

    @Before
    public void createFixedRateTrigger() {
        trigger = new FixedRateTrigger(null, null, null);
    }

    @Test
    public void initialDelay() {
        Duration delay = trigger.initialDelay();
        Assert.assertTrue("delay should be -20% to +20% of 10s", delay.getSeconds() <= 12 && delay.getSeconds() >= 8);
    }
}