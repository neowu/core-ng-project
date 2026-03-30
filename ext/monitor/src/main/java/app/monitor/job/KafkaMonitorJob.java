package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.Severity;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
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
import java.util.Set;

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
    static final ObjectName LOG_SIZE_BEAN = objectName("kafka.log:type=Log,name=Size,topic=*,partition=*");

    private final Logger logger = LoggerFactory.getLogger(KafkaMonitorJob.class);
    private final JMXClient jmxClient;
    private final String app;
    private final String host;
    private final MessagePublisher<StatMessage> publisher;
    private final GCStat youngGCStats = new GCStat("young");
    private final GCStat oldGCStats = new GCStat("old");
    public double highHeapUsageThreshold;
    public long highDiskSizeThreshold;

    public KafkaMonitorJob(JMXClient jmxClient, String app, String host, MessagePublisher<StatMessage> publisher) {
        this.jmxClient = jmxClient;
        this.app = app;
        this.host = host;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        try {
            MBeanServerConnection connection = jmxClient.connect();
            Stats stats = collect(connection);
            publisher.publish(StatMessageFactory.stats(app, host, stats));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            publisher.publish(StatMessageFactory.failedToCollect(app, host, e));
        }
    }

    Stats collect(MBeanServerConnection connection) throws JMException, IOException {
        var stats = new Stats();

        collectDiskUsage(stats, connection);    // disk usage is most important to check, if disk usage is high, requires to expand disk immediately

        CompositeData heap = (CompositeData) connection.getAttribute(MEMORY_BEAN, "HeapMemoryUsage");
        double heapUsed = (Long) heap.get("used");
        stats.put("kafka_heap_used", heapUsed);
        double heapMax = (Long) heap.get("max");
        stats.put("kafka_heap_max", heapMax);
        stats.checkHighUsage(heapUsed / heapMax, highHeapUsageThreshold, "heap");

        CompositeData nonHeap = (CompositeData) connection.getAttribute(MEMORY_BEAN, "NonHeapMemoryUsage");
        double nonHeapUsed = (Long) nonHeap.get("used");
        stats.put("kafka_non_heap_used", nonHeapUsed);

        collectGCStats(stats, connection, youngGCStats, YOUNG_GC_BEAN);
        collectGCStats(stats, connection, oldGCStats, OLD_GC_BEAN);

        stats.put("kafka_bytes_out_rate", (Double) connection.getAttribute(BYTES_OUT_RATE_BEAN, "OneMinuteRate"));
        stats.put("kafka_bytes_in_rate", (Double) connection.getAttribute(BYTES_IN_RATE_BEAN, "OneMinuteRate"));

        return stats;
    }

    private void collectDiskUsage(Stats stats, MBeanServerConnection connection) throws IOException, MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        long diskUsed = 0;
        Set<ObjectName> sizeBeans = connection.queryNames(LOG_SIZE_BEAN, null); // return one object bean per topic/partition
        for (ObjectName sizeBean : sizeBeans) {
            long size = (Long) connection.getAttribute(sizeBean, "Value");
            diskUsed += size;
        }
        stats.put("kafka_disk_used", diskUsed);
        boolean highUsage = stats.checkHighUsage((double) diskUsed / highDiskSizeThreshold, 1.0, "disk");
        if (highUsage) {
            stats.severity = Severity.ERROR;
        }
    }

    private void collectGCStats(Stats stats, MBeanServerConnection connection, GCStat gcStats, ObjectName gcBean) throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        double count = gcStats.count((Long) connection.getAttribute(gcBean, "CollectionCount"));
        double elapsed = gcStats.elapsed((Long) connection.getAttribute(gcBean, "CollectionTime"));
        stats.put("kafka_gc_" + gcStats.name + "_count", count);
        stats.put("kafka_gc_" + gcStats.name + "_elapsed", elapsed);
    }
}
