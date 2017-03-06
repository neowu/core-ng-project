package core.framework.impl.kafka;

import core.framework.api.util.Lists;
import core.framework.impl.log.stat.Metrics;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ConsumerMetrics implements Metrics {
    private final String name;
    private final List<Metric> recordsLagMax = Lists.newArrayList();
    private final List<Metric> recordsConsumedRate = Lists.newArrayList();
    private final List<Metric> bytesConsumedRate = Lists.newArrayList();
    private final List<Metric> fetchRate = Lists.newArrayList();
    private final List<Metric> commitLatencyMax = Lists.newArrayList();

    ConsumerMetrics(String name) {
        this.name = name;
    }

    @Override
    public void collect(Map<String, Double> stats) {
        if (!recordsLagMax.isEmpty()) {
            stats.put(statName("records_max_lag"), sum(recordsLagMax));
            stats.put(statName("records_consumed_rate"), sum(recordsConsumedRate));
            stats.put(statName("bytes_consumed_rate"), sum(bytesConsumedRate));
            stats.put(statName("fetch_rate"), sum(fetchRate));
            stats.put(statName("commit_max_latency"), max(commitLatencyMax));
        }
    }

    void addMetrics(Map<MetricName, ? extends Metric> kafkaMetrics) {
        for (Map.Entry<MetricName, ? extends Metric> entry : kafkaMetrics.entrySet()) {
            MetricName name = entry.getKey();
            if ("consumer-fetch-manager-metrics".equals(name.group())) {
                if ("records-lag-max".equals(name.name())) recordsLagMax.add(entry.getValue());
                else if ("records-consumed-rate".equals(name.name())) recordsConsumedRate.add(entry.getValue());
                else if ("bytes-consumed-rate".equals(name.name())) bytesConsumedRate.add(entry.getValue());
                else if ("fetch-rate".equals(name.name())) fetchRate.add(entry.getValue());
            } else if ("consumer-coordinator-metrics".equals(name.group()) && "commit-latency-max".equals(name.name()))
                commitLatencyMax.add(entry.getValue());
        }
    }

    private double sum(List<Metric> metrics) {
        return metrics.stream().mapToDouble(Metric::value).filter(Double::isFinite).sum();
    }

    private double max(List<Metric> metrics) {
        return metrics.stream().mapToDouble(Metric::value).filter(Double::isFinite).max().orElse(0);
    }

    String statName(String statName) {
        StringBuilder builder = new StringBuilder("kafka_consumer");
        if (name != null) builder.append('_').append(name);
        builder.append('_').append(statName);
        return builder.toString();
    }
}
