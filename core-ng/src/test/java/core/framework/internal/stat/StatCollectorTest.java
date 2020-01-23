package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    void collect() {
        Stats stats = collector.collect();

        assertThat(stats.stats).containsKeys("cpu_usage", "thread_count", "jvm_heap_used", "jvm_heap_max");
    }

    @Test
    void checkHighCPUUsage() {
        collector.highCPUUsageThreshold = 0.8;
        var stats = new Stats();
        collector.checkHighCPUUsage(0.79, stats);
        assertThat(stats.errorCode).isNull();

        collector.checkHighCPUUsage(0.8, stats);
        assertThat(stats.errorCode).isEqualTo("HIGH_CPU_USAGE");
        assertThat(stats.errorMessage).contains("usage=80%");
    }

    @Test
    void checkHighHeapUsage() {
        collector.highHeapUsageThreshold = 0.8;
        var stats = new Stats();
        collector.checkHighHeapUsage(79, 100, stats);
        assertThat(stats.errorCode).isNull();

        collector.checkHighHeapUsage(80, 100, stats);
        assertThat(stats.errorCode).isEqualTo("HIGH_HEAP_USAGE");
        assertThat(stats.errorMessage).contains("usage=80%");
    }
}
