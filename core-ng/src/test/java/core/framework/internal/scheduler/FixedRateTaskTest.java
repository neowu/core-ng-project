package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

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

    @Test
    void scheduleNext() {
        FixedRateTask task = new FixedRateTask(null, null, Duration.ofSeconds(30));
        ZonedDateTime start = ZonedDateTime.now();
        task.scheduledTime = start;
        ZonedDateTime next = task.scheduleNext();

        assertThat(Duration.between(start, next)).isEqualTo(Duration.ofSeconds(30));
    }
}
