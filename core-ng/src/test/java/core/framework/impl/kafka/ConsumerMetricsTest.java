package core.framework.impl.kafka;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class ConsumerMetricsTest {
    @Test
    public void statName() {
        ConsumerMetrics metrics = new ConsumerMetrics("log");
        assertEquals("kafka_consumer_log_lag_max", metrics.statName("lag_max"));

        metrics = new ConsumerMetrics(null);
        assertEquals("kafka_consumer_lag_max", metrics.statName("lag_max"));
    }
}