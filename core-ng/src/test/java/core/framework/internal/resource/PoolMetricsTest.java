package core.framework.internal.resource;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(metrics.statName("size")).isEqualTo("pool_test_size");
    }

    @Test
    void collect() {
        pool.borrowItem();
        pool.returnItem(pool.borrowItem());

        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats)
                .containsEntry("pool_test_active_count", 1.0d)
                .containsEntry("pool_test_total_count", 2.0d);
    }
}
