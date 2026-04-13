package core.framework.search.impl;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticSearchMetricsTest {
    private LogInstrumentation instrumentation;
    private ElasticSearchMetrics metrics;

    @BeforeEach
    void createElasticSearchMetrics() {
        instrumentation = new LogInstrumentation();

        metrics = new ElasticSearchMetrics(null, instrumentation);
    }

    @Test
    void collect() {
        instrumentation.activeRequests.increase();

        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats).containsEntry("es_active_requests", 1.0);
    }
}
