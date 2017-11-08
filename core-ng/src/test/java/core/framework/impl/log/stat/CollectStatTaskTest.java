package core.framework.impl.log.stat;

import core.framework.util.Lists;
import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class CollectStatTaskTest {
    private CollectStatTask task;

    @BeforeEach
    void createCollectStatsTask() {
        task = new CollectStatTask(null, Lists.newArrayList());
    }

    @Test
    void garbageCollectorName() {
        assertEquals("g1_young_generation", task.garbageCollectorName("G1 Young Generation"));
        assertEquals("g1_old_generation", task.garbageCollectorName("G1 Old Generation"));
    }

    @Test
    void collect() {
        Map<String, Double> stats = Maps.newLinkedHashMap();
        task.collect(stats);

        assertNotNull(stats.get("thread_count"));
        assertNotNull(stats.get("jvm_heap_used"));
        assertNotNull(stats.get("jvm_heap_max"));
    }
}
