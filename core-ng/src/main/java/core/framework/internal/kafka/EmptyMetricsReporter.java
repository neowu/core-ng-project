package core.framework.internal.kafka;

import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;

import java.util.List;
import java.util.Map;

public class EmptyMetricsReporter implements MetricsReporter {
    @Override
    public void init(List<KafkaMetric> metrics) {
    }

    @Override
    public void metricChange(KafkaMetric metric) {
    }

    @Override
    public void metricRemoval(KafkaMetric metric) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
