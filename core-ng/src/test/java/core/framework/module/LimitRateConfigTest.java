package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class LimitRateConfigTest {
    private LimitRateConfig config;

    @BeforeEach
    void createLimitRateConfig() {
        config = new LimitRateConfig();
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("limitRate is configured but no group added");
    }
}
