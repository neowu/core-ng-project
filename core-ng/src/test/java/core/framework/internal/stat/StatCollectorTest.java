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
}
