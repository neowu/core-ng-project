package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RedisConfigTest {
    private RedisConfig config;

    @BeforeEach
    void createRedisConfig() {
        config = new RedisConfig();
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("redis host must be configured");
    }
}
