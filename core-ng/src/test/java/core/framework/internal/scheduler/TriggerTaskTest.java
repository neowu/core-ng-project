package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TriggerTaskTest {
    @Test
    void trigger() {
        TriggerTask task = new TriggerTask(null, null, new DailyTrigger(LocalTime.NOON), ZoneId.of("America/New_York"));
        assertThat(task.trigger()).isEqualTo("daily@12:00[America/New_York]");
    }
}
