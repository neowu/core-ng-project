package core.framework.module;

import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class LimitRateConfigTest {
    private LimitRateConfig config;

    @BeforeEach
    void createLimitRateConfig() {
        config = new LimitRateConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
            .hasMessageContaining("limitRate is configured but no group added");
    }

    @Test
    void add() {
        config.add("test", 100, 100, Duration.ofMinutes(1));
        config.validate();
    }
}
