package core.framework.impl.log.stat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class CollectStatTaskTest {
    CollectStatTask task;

    @Before
    public void createCollectStatTask() {
        task = new CollectStatTask(null);
    }

    @Test
    public void garbageCollectorName() {
        Assert.assertEquals("G1YoungGeneration", task.garbageCollectorName("G1 Young Generation"));
        Assert.assertEquals("G1OldGeneration", task.garbageCollectorName("G1 Old Generation"));
    }
}