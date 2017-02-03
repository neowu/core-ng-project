package core.framework.impl.log.stat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class CollectStatsTaskTest {
    @Test
    public void garbageCollectorName() {
        assertEquals("g1_young_generation", CollectStatsTask.garbageCollectorName("G1 Young Generation"));
        assertEquals("g1_old_generation", CollectStatsTask.garbageCollectorName("G1 Old Generation"));
    }
}