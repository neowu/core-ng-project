package core.framework.internal.cache;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LocalCacheMetricsTest {
    private LocalCacheMetrics metrics;

    @BeforeEach
    void createLocalCacheMetrics() {
        metrics = new LocalCacheMetrics(new LocalCacheStore());
    }

    @Test
    void collect() {
        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats)
                .containsEntry("cache_size", 0.0d);
    }
}
