package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class StatTest {
    private Stat stat;

    @BeforeEach
    void createStat() {
        stat = new Stat();
    }

    @Test
    void collect() {
        Map<String, Double> stats = stat.collect();

        assertThat(stats).containsKeys("cpu_usage", "thread_count", "jvm_heap_used", "jvm_heap_max");
    }
}
