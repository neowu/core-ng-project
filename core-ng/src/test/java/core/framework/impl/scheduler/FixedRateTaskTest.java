package core.framework.impl.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

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
