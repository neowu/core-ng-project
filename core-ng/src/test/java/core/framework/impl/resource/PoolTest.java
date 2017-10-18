package core.framework.impl.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class PoolTest {
    private Pool<TestResource> pool;

    @BeforeEach
    void createPool() {
        pool = new Pool<>(TestResource::new, "pool");
    }

    @Test
    void borrowAndReturn() {
        PoolItem<TestResource> item = pool.borrowItem();
        assertNotNull(item.resource);
        pool.returnItem(item);

        assertEquals(1, pool.idleItems.size());
        assertTrue(pool.idleItems.getFirst().returnTime > 0);
    }

    @Test
    void returnBrokenResource() {
        PoolItem<TestResource> item = pool.borrowItem();
        assertNotNull(item.resource);
        item.broken = true;
        pool.returnItem(item);

        assertEquals(0, pool.idleItems.size());
        assertTrue(item.resource.closed);
    }

    @Test
    void refresh() {
        pool.maxIdleTime(Duration.ZERO);
        pool.size(2, 2);

        pool.refresh();
        assertEquals(2, pool.idleItems.size());
    }

    @Test
    void borrowWithTimeout() {
        pool.size(0, 0);
        pool.checkoutTimeout(Duration.ZERO);
        PoolException exception = assertThrows(PoolException.class, () -> pool.borrowItem());
        assertEquals("POOL_TIME_OUT", exception.errorCode());
    }

    @Test
    void close() {
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
