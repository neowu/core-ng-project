package core.framework.internal.kafka;

import org.apache.kafka.common.Metric;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ConsumerMetricsTest {
    @Test
    void statName() {
        ConsumerMetrics metrics = new ConsumerMetrics("log");
        assertEquals("kafka_consumer_log_lag_max", metrics.statName("lag_max"));

        metrics = new ConsumerMetrics(null);
        assertEquals("kafka_consumer_lag_max", metrics.statName("lag_max"));
    }

    @Test
    void sum() {
        Metric metric1 = mock(Metric.class);
        when(metric1.metricValue()).thenReturn(1d);
        Metric metric2 = mock(Metric.class);
        when(metric2.metricValue()).thenReturn(Double.NEGATIVE_INFINITY);

        var metrics = new ConsumerMetrics(null);
        assertThat(metrics.sum(List.of(metric1, metric1, metric2))).isEqualTo(2);
    }
}
