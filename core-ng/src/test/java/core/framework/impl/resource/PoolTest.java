package core.framework.impl.resource;

import org.junit.Assert;
import org.junit.Test;

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
    public void borrowAndReturn() {
        Pool<TestResource> pool = new Pool<>(TestResource::new, TestResource::close);

        PoolItem<TestResource> item = pool.borrowItem();
        Assert.assertNotNull(item.resource);
        pool.returnItem(item);

        Assert.assertEquals(1, pool.queue.size());
        Assert.assertNotNull(pool.queue.getFirst().returnTime);
    }

    @Test
    public void close() {
        Pool<TestResource> pool = new Pool<>(TestResource::new, TestResource::close);

        PoolItem<TestResource> item = pool.borrowItem();
        pool.returnItem(item);

        pool.close();
        Assert.assertTrue(item.resource.closed);
    }
}