package core.framework.internal.stat;

import core.framework.log.Severity;
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

        stats.errorCode = "HIGH_CPU_USAGE";
        assertThat(stats.result()).isEqualTo("WARN");

        stats.severity = Severity.ERROR;
        assertThat(stats.result()).isEqualTo("ERROR");
    }

    @Test
    void checkHighUsage() {
        stats.checkHighUsage(0.7, 0.8, "disk");
        assertThat(stats.errorCode).isNull();

        stats.checkHighUsage(0.81, 0.8, "disk");
        assertThat(stats.errorCode).isEqualTo("HIGH_DISK_USAGE");
        assertThat(stats.errorMessage).isEqualTo("disk usage is too high, usage=81%");
    }

    @Test
    void checkHighUsageWithMultipleWarnings() {
        stats.checkHighUsage(1.0, 0.8, "cpu");
        assertThat(stats.errorCode).isEqualTo("HIGH_CPU_USAGE");

        stats.checkHighUsage(0.8, 0.8, "heap");
        assertThat(stats.errorCode).isEqualTo("HIGH_CPU_USAGE");
    }
}
