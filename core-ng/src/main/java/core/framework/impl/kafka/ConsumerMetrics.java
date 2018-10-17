package core.framework.impl.kafka;

import core.framework.internal.stat.Metrics;
import core.framework.util.Lists;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ConsumerMetrics implements Metrics {
    private final String name;
    private final List<Metric> recordsLagMax = Lists.newArrayList();        // use non-thread-safe list as it's ok to fail once, and the list is initialized on startup
    private final List<Metric> recordsConsumedRate = Lists.newArrayList();
    private final List<Metric> bytesConsumedRate = Lists.newArrayList();
    private final List<Metric> fetchRate = Lists.newArrayList();

    ConsumerMetrics(String name) {
        this.name = name;
    }

    @Override
    public void collect(Map<String, Double> stats) {
        stats.put(statName("records_max_lag"), sum(recordsLagMax));
        stats.put(statName("records_consumed_rate"), sum(recordsConsumedRate));
        stats.put(statName("bytes_consumed_rate"), sum(bytesConsumedRate));
        stats.put(statName("fetch_rate"), sum(fetchRate));
    }

    void add(Map<MetricName, ? extends Metric> kafkaMetrics) {
        for (var entry : kafkaMetrics.entrySet()) {
            MetricName name = entry.getKey();
            if ("consumer-fetch-manager-metrics".equals(name.group())) {
                if ("records-lag-max".equals(name.name())) recordsLagMax.add(entry.getValue());
                else if ("records-consumed-rate".equals(name.name())) recordsConsumedRate.add(entry.getValue());
                else if ("bytes-consumed-rate".equals(name.name())) bytesConsumedRate.add(entry.getValue());
                else if ("fetch-rate".equals(name.name())) fetchRate.add(entry.getValue());
            }
        }
    }

    double sum(List<Metric> metrics) {
        double sum = 0.0;
        for (var metric : metrics) {
            double value = (double) metric.metricValue();
            if (Double.isFinite(value)) sum += value; // kafka uses infinity as initial value, e.g. org.apache.kafka.common.metrics.stats.Max
        }
        return sum;
    }

    String statName(String statName) {
        var builder = new StringBuilder("kafka_consumer");
        if (name != null) builder.append('_').append(name);
        builder.append('_').append(statName);
        return builder.toString();
    }
}
