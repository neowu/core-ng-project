package core.framework.internal.kafka;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.Map;

/**
 * @author neo
 */
public class ProducerMetrics implements Metrics {
    private final String name;
    Metric requestSizeAvg; // The average size of all requests in the window for a broker.
    Metric requestSizeMax; // The maximum size of any request sent in the window for a broker
    private Metric requestRate; // The number of batch requests sent per second, one batch request contains multiple messages
    private Metric outgoingByteRate; // The number of outgoing bytes sent to all servers per second

    public ProducerMetrics(String name) {
        this.name = name;
    }

    @Override
    public void collect(Stats stats) {
        if (requestRate != null) stats.put(statName("request_rate"), (Double) requestRate.metricValue());
        if (requestSizeAvg != null) {
            Double value = (Double) requestSizeAvg.metricValue();
            if (!value.isNaN()) stats.put(statName("request_size_avg"), value);     // avg value can be NaN, refer to https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=95652345
        }
        if (requestSizeMax != null) {
            Double value = (Double) requestSizeMax.metricValue();
            if (!value.isNaN()) stats.put(statName("request_size_max"), value);
        }
        if (outgoingByteRate != null) stats.put(statName("outgoing_byte_rate"), (Double) outgoingByteRate.metricValue());
    }

    // refer to https://docs.confluent.io/platform/current/kafka/monitoring.html#per-broker-metrics
    @SuppressWarnings("PMD.UnusedAssignment")   // false positive
    public void set(Map<MetricName, ? extends Metric> kafkaMetrics) {
        for (Map.Entry<MetricName, ? extends Metric> entry : kafkaMetrics.entrySet()) {
            MetricName metricName = entry.getKey();
            if ("producer-metrics".equals(metricName.group())) {
                String name = metricName.name();
                if ("request-rate".equals(name)) requestRate = entry.getValue();
                else if ("request-size-avg".equals(name)) requestSizeAvg = entry.getValue();
                else if ("request-size-max".equals(name)) requestSizeMax = entry.getValue();
                else if ("outgoing-byte-rate".equals(name)) outgoingByteRate = entry.getValue();
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
