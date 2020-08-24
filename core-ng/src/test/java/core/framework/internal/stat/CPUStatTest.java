package core.framework.internal.stat;

import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CPUStatTest {
    private CPUStat stat;

    @BeforeEach
    void createCPUStat() {
        stat = new CPUStat((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
    }

    @Test
    void usage() {
        double usage = stat.usage();
        assertThat(usage).isGreaterThanOrEqualTo(0);
    }

    @Test
    void calculateUsage() {
        // 1 cpu, cpu uses 100ns, elapsed 100ns
        assertThat(stat.usage(200L, 100L, 200L, 100L, 1))
                .isEqualTo(1);

        // 2 cpus, cpu uses 100ns, elapsed 200ns
        assertThat(stat.usage(300L, 100L, 200L, 100L, 2))
                .isEqualTo(0.25);
    }
}
