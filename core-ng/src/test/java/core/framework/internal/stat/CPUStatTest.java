package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CPUStatTest {
    private CPUStat stat;

    @BeforeEach
    void createCPUStat() {
        stat = new CPUStat(ManagementFactory.getThreadMXBean(), 2);
    }

    @Test
    void usage() {
        double usage = stat.usage();
        assertThat(usage).isGreaterThan(0);
    }

    @Test
    void calculateUsage() {
        // 2 cpus, thread 1 used 100, thread 2 used 100, elapsed time = 100
        assertThat(stat.usage(Map.of(1L, 200L, 2L, 100L), 200, Map.of(1L, 100L), 100))
                .isEqualTo(1);

        // 2 cpus, thread 1 used 50, elapsed time = 200
        assertThat(stat.usage(Map.of(1L, 200L), 300, Map.of(1L, 150L), 100))
                .isEqualTo(0.125);
    }
}
