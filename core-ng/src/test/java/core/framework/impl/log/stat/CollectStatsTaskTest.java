package core.framework.impl.log.stat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class CollectStatsTaskTest {
    @Test
    void garbageCollectorName() {
        assertEquals("g1_young_generation", CollectStatsTask.garbageCollectorName("G1 Young Generation"));
        assertEquals("g1_old_generation", CollectStatsTask.garbageCollectorName("G1 Old Generation"));
    }
}
