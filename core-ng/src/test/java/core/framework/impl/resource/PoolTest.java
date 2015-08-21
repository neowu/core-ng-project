package core.framework.impl.resource;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * @author neo
 */
public class PoolTest {
    static class TestResource implements AutoCloseable {
        boolean closed;

        @Override
        public void close() throws Exception {
            closed = true;
        }
    }

    @Test
    public void takeAndReturn() {
        Pool<TestResource> pool = new Pool<>(TestResource::new, 0, 5, Duration.ofSeconds(1));

        try (PoolItem<TestResource> item = pool.take()) {
            Assert.assertNotNull(item.resource);
        }

        Assert.assertEquals(1, pool.queue.size());
        Assert.assertNotNull(pool.queue.getFirst().returnTime);
    }

    @Test
    public void close() {
        Pool<TestResource> pool = new Pool<>(TestResource::new, 0, 5, Duration.ofSeconds(1));

        TestResource resource;
        try (PoolItem<TestResource> item = pool.take()) {
            resource = item.resource;
        }

        pool.close();
        Assert.assertTrue(resource.closed);
    }
}