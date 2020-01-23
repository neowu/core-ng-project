package core.framework.internal.kafka;

import core.framework.internal.stat.Stats;
import org.apache.kafka.common.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ProducerMetricsTest {
    private ProducerMetrics metrics;
    private Metric requestSizeAvg;

    @BeforeEach
    void createProducerMetrics() {
        requestSizeAvg = mock(Metric.class);

        metrics = new ProducerMetrics(null);
        metrics.requestSizeAvg = requestSizeAvg;
    }

    @Test
    void collect() {
        when(requestSizeAvg.metricValue()).thenReturn(10.0);

        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats).containsEntry("kafka_producer_request_size_avg", 10.0);
    }

    @Test
    void collectWithoutRequestSizeAvg() {
        when(requestSizeAvg.metricValue()).thenReturn(Double.NaN);

        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats).doesNotContainKeys("kafka_producer_request_size_avg");
    }
}
