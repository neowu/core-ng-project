package core.framework.impl.log.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void garbageCollectorName() {
        assertEquals("g1_young_generation", stat.garbageCollectorName("G1 Young Generation"));
        assertEquals("g1_old_generation", stat.garbageCollectorName("G1 Old Generation"));
    }

    @Test
    void collect() {
        Map<String, Double> stats = stat.collect();

        assertNotNull(stats.get("thread_count"));
        assertNotNull(stats.get("jvm_heap_used"));
        assertNotNull(stats.get("jvm_heap_max"));
    }
}
