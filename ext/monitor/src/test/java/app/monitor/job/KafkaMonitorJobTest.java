package app.monitor.job;

import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Set;

import static app.monitor.job.JMXClient.objectName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class KafkaMonitorJobTest {
    @Mock
    JMXClient jmxClient;
    @Mock
    MessagePublisher<StatMessage> publisher;
    @Mock
    MBeanServerConnection connection;
    private KafkaMonitorJob job;

    @BeforeEach
    void createKafkaMonitorJob() {
        job = new KafkaMonitorJob(jmxClient, "kafka", "localhost", publisher);
        job.highDiskSizeThreshold = 10_000_000;
    }

    @Test
    void collect() throws JMException, IOException {
        CompositeData heap = mock(CompositeData.class);
        when(heap.get("used")).thenReturn(1000L);
        when(heap.get("max")).thenReturn(2000L);
        when(connection.getAttribute(KafkaMonitorJob.MEMORY_BEAN, "HeapMemoryUsage")).thenReturn(heap);
        when(connection.getAttribute(KafkaMonitorJob.MEMORY_BEAN, "NonHeapMemoryUsage")).thenReturn(heap);
        when(connection.getAttribute(KafkaMonitorJob.OLD_GC_BEAN, "CollectionCount")).thenReturn(0L);
        when(connection.getAttribute(KafkaMonitorJob.OLD_GC_BEAN, "CollectionTime")).thenReturn(0L);
        when(connection.getAttribute(KafkaMonitorJob.YOUNG_GC_BEAN, "CollectionCount")).thenReturn(1L);
        when(connection.getAttribute(KafkaMonitorJob.YOUNG_GC_BEAN, "CollectionTime")).thenReturn(100L);
        when(connection.getAttribute(KafkaMonitorJob.BYTES_OUT_RATE_BEAN, "OneMinuteRate")).thenReturn(10D);
        when(connection.getAttribute(KafkaMonitorJob.BYTES_IN_RATE_BEAN, "OneMinuteRate")).thenReturn(10D);
        ObjectName sizeBean = objectName("kafka.log:type=Log,name=Size,topic=product-updated,partition=0");
        when(connection.queryNames(KafkaMonitorJob.LOG_SIZE_BEAN, null)).thenReturn(Set.of(sizeBean));
        when(connection.getAttribute(sizeBean, "Value")).thenReturn(12_000_000L);

        Stats stats = job.collect(connection);
        assertThat(stats.stats).containsKeys("kafka_heap_used", "kafka_heap_max", "kafka_non_heap_used",
                "kafka_gc_young_count", "kafka_gc_young_elapsed",
                "kafka_gc_old_count", "kafka_gc_old_elapsed",
                "kafka_bytes_out_rate", "kafka_bytes_in_rate")
            .containsEntry("kafka_disk_used", 12_000_000.0);

        assertThat(stats.errorCode).isEqualTo("HIGH_DISK_USAGE");
        assertThat(stats.errorMessage).isEqualTo("disk usage is too high, usage=120%");
    }

    @Test
    void publishError() throws IOException {
        when(jmxClient.connect()).thenThrow(new IOException("mock"));
        job.execute(null);
        verify(publisher).publish(argThat(message -> "kafka".equals(message.app)
                                                     && "ERROR".equals(message.result)
                                                     && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
