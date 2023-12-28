package core.framework.module;

import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RedisConfigTest {
    private RedisConfig config;

    @BeforeEach
    void createRedisConfig() {
        config = new RedisConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
            .hasMessageContaining("redis host must be configured");
    }

    @Test
    void client() {
        config.host("localhost");
        config.poolSize(0, 0);
        config.timeout(Duration.ofSeconds(5));
        assertThat(config.client()).isNotNull();

        config.validate();
    }
}
