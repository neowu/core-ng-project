package core.framework.internal.kafka;

import core.framework.internal.stat.Stats;
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
        var metrics = new ConsumerMetrics("log");
        assertEquals("kafka_consumer_log_lag_max", metrics.statName("lag_max"));

        metrics = new ConsumerMetrics(null);
        assertEquals("kafka_consumer_lag_max", metrics.statName("lag_max"));
    }

    @Test
    void sum() {
        var metric1 = mock(Metric.class);
        when(metric1.metricValue()).thenReturn(1d);
        var metric2 = mock(Metric.class);
        when(metric2.metricValue()).thenReturn(Double.NEGATIVE_INFINITY);

        var metrics = new ConsumerMetrics(null);
        assertThat(metrics.sum(List.of(metric1, metric1, metric2))).isEqualTo(2);
    }

    @Test
    void collect() {
        var metrics = new ConsumerMetrics(null);
        var stats = new Stats();
        metrics.collect(stats);

        assertThat(stats.stats).containsKeys("kafka_consumer_records_max_lag",
                "kafka_consumer_records_consumed_rate",
                "kafka_consumer_bytes_consumed_rate",
                "kafka_consumer_fetch_rate");
    }
}
