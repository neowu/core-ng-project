package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class FixedRateTriggerTest {
    @Test
    void frequency() {
        FixedRateTrigger trigger = new FixedRateTrigger(null, null, Duration.ofSeconds(30));

        assertEquals("fixedRate@PT30S", trigger.frequency());
    }
}
