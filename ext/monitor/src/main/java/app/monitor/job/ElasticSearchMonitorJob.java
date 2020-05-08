package app.monitor.job;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.log.LogManager;
import core.framework.internal.stat.Stats;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.util.Strings;
import core.framework.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ElasticSearchMonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchMonitorJob.class);
    private final HTTPClient httpClient;
    private final String app;
    private final String host;
    private final MessagePublisher<StatMessage> publisher;
    public double highHeapUsageThreshold;
    public double highDiskUsageThreshold;

    public ElasticSearchMonitorJob(HTTPClient httpClient, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.httpClient = httpClient;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        try {
            double count = collectCount();
            // refer to org.elasticsearch.rest.action.cat.RestNodesAction for all available fields
            List<Map<String, String>> nodeValues = cat("nodes", "name", "disk.used", "disk.total", "heap.current", "heap.max");
            for (Map<String, String> values : nodeValues) {
                String host = values.get("name");
                Stats stats = collect(values, count);
                publishStats(host, stats);
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            publishError(e);
        }
    }

    Stats collect(Map<String, String> values, double count) {
        var stats = new Stats();

        double heapUsed = get(values, "heap.current");
        stats.put("es_heap_used", heapUsed);
        double heapMax = get(values, "heap.max");
        stats.put("es_heap_max", heapMax);
        stats.checkHighUsage(heapUsed / heapMax, highHeapUsageThreshold, "heap");

        double diskUsed = get(values, "disk.used");
        stats.put("es_disk_used", diskUsed);
        double diskMax = get(values, "disk.total");
        stats.put("es_disk_max", diskMax);
        stats.checkHighUsage(diskUsed / diskMax, highDiskUsageThreshold, "disk");

        stats.put("es_docs", count);
        return stats;
    }

    private void publishStats(String host, Stats stats) {
        Instant now = Instant.now();
        var message = new StatMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.app = app;
        message.host = host;
        message.result = stats.result();
        message.stats = stats.stats;
        message.errorCode = stats.errorCode;
        message.errorMessage = stats.errorMessage;
        publisher.publish(message);
    }

    double collectCount() {
        Map<String, String> values = cat("count", "count").get(0);
        return get(values, "count");
    }

    private void publishError(Throwable e) {
        Instant now = Instant.now();
        var message = new StatMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.result = "ERROR";
        message.app = app;
        message.host = host;
        message.errorCode = "FAILED_TO_COLLECT";
        message.errorMessage = e.getMessage();
        publisher.publish(message);
    }

    private double get(Map<String, String> values, String field) {
        String value = values.get(field);
        if (value == null) throw new Error("can not find field, field=" + field);
        return Double.parseDouble(value);
    }

    private List<Map<String, String>> cat(String command, String... fields) {
        var request = new HTTPRequest(HTTPMethod.GET, "http://" + host + ":9200/_cat/" + command);
        request.params.put("bytes", "b");
        request.params.put("format", "json");
        request.params.put("h", String.join(",", fields));
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.OK.code)
            throw new Error(Strings.format("failed to call es cat api, uri={}, status={}", request.requestURI(), response.statusCode));
        @SuppressWarnings("unchecked")
        List<Map<String, String>> results = (List<Map<String, String>>) JSON.fromJSON(Types.list(Types.map(String.class, String.class)), response.text());
        return results;
    }
}
