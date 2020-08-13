package app.monitor.job;

import core.framework.internal.log.LogManager;
import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class ElasticSearchMonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchMonitorJob.class);
    private final ElasticSearchClient elasticSearchClient;
    private final MessagePublisher<StatMessage> publisher;
    private final String app;
    private final String host;
    private final Map<String, GCStat> gcStats = Maps.newHashMapWithExpectedSize(2);
    public double highHeapUsageThreshold;
    public double highDiskUsageThreshold;

    public ElasticSearchMonitorJob(ElasticSearchClient elasticSearchClient, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.elasticSearchClient = elasticSearchClient;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        try {
            ElasticSearchNodeStats nodeStats = elasticSearchClient.stats(host);
            for (ElasticSearchNodeStats.Node node : nodeStats.nodes.values()) {
                String host = node.name;
                Stats stats = collect(node);
                publishStats(host, stats);
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            publishError(e);
        }
    }

    Stats collect(ElasticSearchNodeStats.Node node) {
        var stats = new Stats();

        double heapUsed = node.jvm.mem.heapUsedInBytes;
        stats.put("es_heap_used", heapUsed);
        double heapMax = node.jvm.mem.heapMaxInBytes;
        stats.put("es_heap_max", heapMax);
        stats.checkHighUsage(heapUsed / heapMax, highHeapUsageThreshold, "heap");
        stats.put("es_non_heap_used", node.jvm.mem.nonHeapUsedInBytes);

        for (Map.Entry<String, ElasticSearchNodeStats.Collector> entry : node.jvm.gc.collectors.entrySet()) {
            ElasticSearchNodeStats.Collector collector = entry.getValue();
            GCStat gcStat = gcStats.computeIfAbsent(entry.getKey(), GCStat::new);
            long count = gcStat.count(collector.collectionCount);
            long elapsed = gcStat.elapsed(collector.collectionTimeInMillis);
            stats.put("es_gc_" + gcStat.name + "_count", (double) count);
            stats.put("es_gc_" + gcStat.name + "_elapsed", (double) elapsed);
        }

        double diskUsed = node.fs.total.totalInBytes - node.fs.total.freeInBytes;
        stats.put("es_disk_used", diskUsed);
        double diskMax = node.fs.total.totalInBytes;
        stats.put("es_disk_max", diskMax);
        stats.checkHighUsage(diskUsed / diskMax, highDiskUsageThreshold, "disk");

        stats.put("es_docs", node.indices.docs.count);
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
        message.info = Map.of("stack_trace", Exceptions.stackTrace(e));
        publisher.publish(message);
    }
}
