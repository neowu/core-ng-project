package core.framework.impl.log.stat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class CollectStatTaskTest {
    @Test
    public void garbageCollectorName() {
        assertEquals("g1_young_generation", CollectStatTask.garbageCollectorName("G1 Young Generation"));
        assertEquals("g1_old_generation", CollectStatTask.garbageCollectorName("G1 Old Generation"));
    }
}