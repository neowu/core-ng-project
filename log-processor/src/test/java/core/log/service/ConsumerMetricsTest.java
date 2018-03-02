package core.log.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ConsumerMetricsTest {
    private ConsumerMetrics metrics;

    @BeforeEach
    void createConsumerMetrics() {
        metrics = new ConsumerMetrics();
    }

    @Test
    void stat() {
        assertThat(metrics.stat(Double.POSITIVE_INFINITY)).isEqualTo(0);
        assertThat(metrics.stat(Double.NEGATIVE_INFINITY)).isEqualTo(0);
        assertThat(metrics.stat(1d)).isEqualTo(1);
    }
}
