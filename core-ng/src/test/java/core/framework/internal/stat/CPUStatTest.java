package core.framework.internal.stat;

import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class CPUStatTest {
    @Mock
    OperatingSystemMXBean os;

    private CPUStat stat;

    @BeforeEach
    void createCPUStat() {
        when(os.getAvailableProcessors()).thenReturn(1);
        stat = new CPUStat(os);
    }

    @Test
    void usage() {
        when(os.getProcessCpuTime()).thenReturn(1000L).thenReturn(2000L);

        double usage = stat.usage();
        assertThat(usage).isEqualTo(0); // first data point should be 0

        usage = stat.usage();
        assertThat(usage).isGreaterThan(0);
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
