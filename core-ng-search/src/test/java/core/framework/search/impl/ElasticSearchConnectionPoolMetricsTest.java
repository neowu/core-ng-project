package core.framework.search.impl;

import core.framework.internal.stat.Stats;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.PoolStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSearchConnectionPoolMetricsTest {
    @Mock
    ConnPoolStats<HttpRoute> poolStats;
    private ElasticSearchConnectionPoolMetrics metrics;

    @BeforeEach
    void createElasticSearchConnectionPoolMetrics() {
        metrics = new ElasticSearchConnectionPoolMetrics(null);
        metrics.poolStats = poolStats;
    }

    @Test
    void collect() {
        when(poolStats.getTotalStats()).thenReturn(new PoolStats(5, 0, 0, 100));

        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats).containsEntry("pool_es_active_count", 5.0)
            .containsEntry("pool_es_total_count", 100.0);
    }
}
