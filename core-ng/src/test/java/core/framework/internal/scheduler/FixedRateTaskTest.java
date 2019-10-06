package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author neo
 */
class FixedRateTaskTest {
    @Test
    void trigger() {
        FixedRateTask task = new FixedRateTask(null, null, Duration.ofSeconds(30));

        assertThat(task.trigger()).isEqualTo("fixedRate@PT30S");
    }
}
