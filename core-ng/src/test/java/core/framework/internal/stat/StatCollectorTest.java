package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

/**
 * @author neo
 */
class StatCollectorTest {
    private StatCollector collector;

    @BeforeEach
    void createStatCollector() {
        collector = new StatCollector();
    }

    @Test
    void collectJVMUsage() {
        var stats = new Stats();
        collector.collectJVMUsage(stats);

        assertThat(stats.stats).containsKeys("cpu_usage", "thread_count", "jvm_heap_used", "jvm_heap_max", "jvm_non_heap_used");
    }

    @Test
    void parseVmRSS() {
        long vmRSS = collector.parseVmRSS("913415 52225 7215 1 0 66363 0");
        assertThat(vmRSS).isEqualTo(52225 * 4096);

        // the vmRSS is over Int.MAX_VALUE
        vmRSS = collector.parseVmRSS("1555592 804536 6204 2 0 823015 0");
        assertThat(vmRSS).isEqualTo(804536L * 4096);
    }

    @Test
    void collectMetrics() {
        Metrics metrics = Mockito.mock(Metrics.class);
        collector.metrics.add(metrics);

        var stats = new Stats();
        doThrow(new Error("test")).when(metrics).collect(stats);

        collector.collectMetrics(stats);
    }
}
