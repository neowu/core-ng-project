package core.framework.impl.resource;

import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class PoolTest {
    private Pool<TestPoolResource> pool;

    @BeforeEach
    void createPool() {
        pool = new Pool<>(TestPoolResource::new, "pool");
        pool.maxIdleTime = Duration.ZERO;
        pool.checkoutTimeout(Duration.ZERO);
    }

    @Test
    void borrowAndReturn() {
        PoolItem<TestPoolResource> item = pool.borrowItem();
        assertNotNull(item.resource);
        pool.returnItem(item);

        assertEquals(1, pool.idleItems.size());
        assertTrue(pool.idleItems.getFirst().returnTime > 0);
    }

    @Test
    void returnBrokenResource() {
        PoolItem<TestPoolResource> item = pool.borrowItem();
        assertNotNull(item.resource);
        item.broken = true;
        pool.returnItem(item);

        assertEquals(0, pool.idleItems.size());
        assertTrue(item.resource.closed);
    }

    @Test
    void refresh() {
        pool.size(2, 2);

        pool.refresh();
        assertEquals(2, pool.idleItems.size());
    }

    @Test
    void refreshWithRecycle() {
        pool.size(1, 5);

        List<PoolItem<TestPoolResource>> items = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            items.add(pool.borrowItem());
        }
        items.forEach(pool::returnItem);

        pool.refresh();
        assertEquals(1, pool.idleItems.size());
    }

    @Test
    void borrowWithTimeout() {
        pool.size(0, 0);

        PoolException exception = assertThrows(PoolException.class, () -> pool.borrowItem());
        assertEquals("POOL_TIME_OUT", exception.errorCode());
    }

    @Test
    void close() {
        PoolItem<TestPoolResource> item = pool.borrowItem();
        pool.returnItem(item);

        pool.close();
        assertTrue(item.resource.closed);
    }
}
