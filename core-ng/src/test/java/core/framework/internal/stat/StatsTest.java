package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class StatsTest {
    private Stats stats;

    @BeforeEach
    void createStats() {
        stats = new Stats();
    }

    @Test
    void result() {
        assertThat(stats.result()).isEqualTo("OK");

        stats.warn("HIGH_CPU_USAGE", "cpu usage is too high, usage=80%");
        assertThat(stats.result()).isEqualTo("WARN");
    }

    @Test
    void warn() {
        stats.warn("WARN1", "message1");
        stats.warn("WARN2", "message2");
        assertThat(stats.errorCode).isEqualTo("WARN1");
    }
}
