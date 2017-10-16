package core.framework.impl.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
