package core.framework.impl.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class PoolTest {
    @Test
    void borrowAndReturn() {
        Pool<TestResource> pool = new Pool<>(TestResource::new);

        PoolItem<TestResource> item = pool.borrowItem();
        assertNotNull(item.resource);
        pool.returnItem(item);

        assertEquals(1, pool.idleItems.size());
        assertTrue(pool.idleItems.getFirst().returnTime > 0);
    }

    @Test
    void close() {
        Pool<TestResource> pool = new Pool<>(TestResource::new);

        PoolItem<TestResource> item = pool.borrowItem();
        pool.returnItem(item);

        pool.close();
        assertTrue(item.resource.closed);
    }

    static class TestResource implements AutoCloseable {
        boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }
}
