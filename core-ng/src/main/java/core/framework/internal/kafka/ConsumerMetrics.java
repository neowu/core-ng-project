package core.framework.internal.kafka;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author neo
 */
public class ConsumerMetrics implements Metrics {
    private final String name;
    private final List<Metric> recordsLagMax = new CopyOnWriteArrayList<>();    // all metrics are added in message thread, so to use concurrent list
    private final List<Metric> recordsConsumedRate = new CopyOnWriteArrayList<>();
    private final List<Metric> bytesConsumedRate = new CopyOnWriteArrayList<>();
    private final List<Metric> fetchRate = new CopyOnWriteArrayList<>();

    ConsumerMetrics(String name) {
        this.name = name;
    }

    @Override
    public void collect(Stats stats) {
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
