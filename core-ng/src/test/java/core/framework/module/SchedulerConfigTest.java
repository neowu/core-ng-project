package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class SchedulerConfigTest {
    private SchedulerConfig config;

    @BeforeEach
    void createSchedulerConfig() {
        config = new SchedulerConfig();
    }

    @Test
    void validateJob() {
        assertThatThrownBy(() -> config.validateJob("job", context -> {
        })).isInstanceOf(Error.class)
                .hasMessageContaining("job class must not be anonymous class or lambda");
    }
}
