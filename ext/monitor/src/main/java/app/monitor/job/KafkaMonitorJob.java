package app.monitor.job;

import core.framework.internal.log.LogManager;
import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static app.monitor.job.JMXClient.objectName;

/**
 * @author neo
 */
public class KafkaMonitorJob implements Job {
    static final ObjectName MEMORY_BEAN = objectName("java.lang:type=Memory");
    static final ObjectName YOUNG_GC_BEAN = objectName("java.lang:name=G1 Young Generation,type=GarbageCollector");
    static final ObjectName OLD_GC_BEAN = objectName("java.lang:name=G1 Old Generation,type=GarbageCollector");
    static final ObjectName BYTES_IN_RATE_BEAN = objectName("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec");
    static final ObjectName BYTES_OUT_RATE_BEAN = objectName("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec");

    private final Logger logger = LoggerFactory.getLogger(KafkaMonitorJob.class);
    private final JMXClient jmxClient;
    private final String app;
    private final String host;
    private final MessagePublisher<StatMessage> publisher;
    private final GCStat youngGCStats = new GCStat("young");
    private final GCStat oldGCStats = new GCStat("old");
    public double highHeapUsageThreshold;

    public KafkaMonitorJob(JMXClient jmxClient, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.jmxClient = jmxClient;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.app = app;
        message.host = host;
        try {
            MBeanServerConnection connection = jmxClient.connect();
            Stats stats = collect(connection);
            message.result = stats.result();
            message.stats = stats.stats;
            message.errorCode = stats.errorCode;
            message.errorMessage = stats.errorMessage;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            message.result = "ERROR";
            message.errorCode = "FAILED_TO_COLLECT";
            message.errorMessage = e.getMessage();
            message.info = Map.of("stack_trace", Exceptions.stackTrace(e));
        }
        publisher.publish(message);
    }

    Stats collect(MBeanServerConnection connection) throws JMException, IOException {
        var stats = new Stats();

        CompositeData heap = (CompositeData) connection.getAttribute(MEMORY_BEAN, "HeapMemoryUsage");
        double heapUsed = (Long) heap.get("used");
        stats.put("kafka_heap_used", heapUsed);
        double heapMax = (Long) heap.get("max");
        stats.put("kafka_heap_max", heapMax);
        stats.checkHighUsage(heapUsed / heapMax, highHeapUsageThreshold, "heap");

        collectGCStats(stats, connection, youngGCStats, YOUNG_GC_BEAN);
        collectGCStats(stats, connection, oldGCStats, OLD_GC_BEAN);

        stats.put("kafka_bytes_out_rate", (Double) connection.getAttribute(BYTES_OUT_RATE_BEAN, "OneMinuteRate"));
        stats.put("kafka_bytes_in_rate", (Double) connection.getAttribute(BYTES_IN_RATE_BEAN, "OneMinuteRate"));

        return stats;
    }

    private void collectGCStats(Stats stats, MBeanServerConnection connection, GCStat gcStats, ObjectName gcBean) throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        double count = gcStats.count((Long) connection.getAttribute(gcBean, "CollectionCount"));
        double elapsed = gcStats.elapsed((Long) connection.getAttribute(gcBean, "CollectionTime"));
        stats.put("kafka_gc_" + gcStats.name + "_count", count);
        stats.put("kafka_gc_" + gcStats.name + "_elapsed", elapsed);
    }
}
