package core.log.service;

import core.framework.impl.log.stat.Metrics;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.Map;

/**
 * @author neo
 */
public class ConsumerMetrics implements Metrics {
    private Metric recordsLagMax;
    private Metric recordsConsumedRate;
    private Metric bytesConsumedRate;
    private Metric fetchRate;

    @Override
    public void collect(Map<String, Double> stats) {
        if (recordsLagMax != null) stats.put("kafka_consumer_records_max_lag", (Double) recordsLagMax.metricValue());
        if (recordsConsumedRate != null) stats.put("kafka_consumer_records_consumed_rate", (Double) recordsConsumedRate.metricValue());
        if (bytesConsumedRate != null) stats.put("kafka_consumer_bytes_consumed_rate", (Double) bytesConsumedRate.metricValue());
        if (fetchRate != null) stats.put("kafka_consumer_fetch_rate", (Double) fetchRate.metricValue());
    }

    void set(Map<MetricName, ? extends Metric> kafkaMetrics) {
        for (Map.Entry<MetricName, ? extends Metric> entry : kafkaMetrics.entrySet()) {
            MetricName name = entry.getKey();
            if ("consumer-fetch-manager-metrics".equals(name.group())) {
                if ("records-lag-max".equals(name.name())) recordsLagMax = entry.getValue();
                else if ("records-consumed-rate".equals(name.name())) recordsConsumedRate = entry.getValue();
                else if ("bytes-consumed-rate".equals(name.name())) bytesConsumedRate = entry.getValue();
                else if ("fetch-rate".equals(name.name())) fetchRate = entry.getValue();
            }
        }
    }
}
