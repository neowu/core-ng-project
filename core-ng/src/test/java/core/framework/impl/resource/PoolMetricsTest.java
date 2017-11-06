package core.framework.impl.resource;

import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class PoolMetricsTest {
    private Pool<TestPoolResource> pool;
    private PoolMetrics metrics;

    @BeforeEach
    void createPoolMetrics() {
        pool = new Pool<>(TestPoolResource::new, "test");
        metrics = new PoolMetrics(pool);
    }

    @Test
    void statName() {
        assertEquals("pool_test_size", metrics.statName("size"));
    }

    @Test
    void collect() {
        pool.borrowItem();
        pool.returnItem(pool.borrowItem());

        Map<String, Double> stats = Maps.newHashMap();
        metrics.collect(stats);

        assertEquals(1, stats.get("pool_test_active_count").intValue());
        assertEquals(2, stats.get("pool_test_total_count").intValue());
    }
}
