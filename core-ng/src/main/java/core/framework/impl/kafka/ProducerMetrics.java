package core.framework.impl.kafka;

import core.framework.impl.log.stat.Metrics;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.Map;

/**
 * @author neo
 */
public class ProducerMetrics implements Metrics {
    private final String name;
    private Metric requestRate;
    private Metric outgoingByteRate;

    public ProducerMetrics(String name) {
        this.name = name;
    }

    @Override
    public void collect(Map<String, Double> stats) {
        if (requestRate != null) stats.put(statName("request_rate"), (Double) requestRate.metricValue());
        if (outgoingByteRate != null) stats.put(statName("outgoing_byte_rate"), (Double) outgoingByteRate.metricValue());
    }

    public void set(Map<MetricName, ? extends Metric> kafkaMetrics) {
        for (Map.Entry<MetricName, ? extends Metric> entry : kafkaMetrics.entrySet()) {
            MetricName name = entry.getKey();
            if ("producer-metrics".equals(name.group())) {
                if ("request-rate".equals(name.name())) requestRate = entry.getValue();
                else if ("outgoing-byte-rate".equals(name.name())) outgoingByteRate = entry.getValue();
            }
        }
    }

    private String statName(String statName) {
        StringBuilder builder = new StringBuilder("kafka_producer");
        if (name != null) builder.append('_').append(name);
        builder.append('_').append(statName);
        return builder.toString();
    }
}
