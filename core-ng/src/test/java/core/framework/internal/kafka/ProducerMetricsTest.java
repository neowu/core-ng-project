package core.framework.internal.kafka;

import org.apache.kafka.common.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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

        Map<String, Double> stats = new HashMap<>();
        metrics.collect(stats);

        assertThat(stats).containsEntry("kafka_producer_request_size_avg", 10.0);
    }

    @Test
    void collectWithoutRequestSizeAvg() {
        when(requestSizeAvg.metricValue()).thenReturn(Double.NaN);

        Map<String, Double> stats = new HashMap<>();
        metrics.collect(stats);

        assertThat(stats).doesNotContainKeys("kafka_producer_request_size_avg");
    }
}
