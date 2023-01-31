package app.monitor.job;

import com.mongodb.MongoException;
import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class MongoMonitorJobTest {
    @Mock
    MessagePublisher<StatMessage> publisher;
    private MongoMonitorJob job;

    @BeforeEach
    void createMongoMonitorJob() {
        job = Mockito.spy(new MongoMonitorJob(null, "mongo", "host", publisher));
    }

    @Test
    void collect() {
        doReturn(List.of("local", "admin")).when(job).listDB();
        doReturn(new Document(Map.of("objects", 1, "totalSize", 10D, "fsUsedSize", 20D, "fsTotalSize", 50D))).when(job).dbStats("local");
        doReturn(new Document(Map.of("objects", 5, "totalSize", 15D, "fsUsedSize", 20D, "fsTotalSize", 50D))).when(job).dbStats("admin");

        Stats stats = job.collect();
        assertThat(stats.stats).containsEntry("mongo_docs", 6D);
        assertThat(stats.stats).containsEntry("mongo_total_size", 25D);
        assertThat(stats.stats).containsEntry("mongo_disk_used", 20D);
        assertThat(stats.stats).containsEntry("mongo_disk_max", 50D);
    }

    @Test
    void publishError() {
        doThrow(new MongoException("Connection refused")).when(job).listDB();
        job.execute(null);
        verify(publisher).publish(argThat(message -> "mongo".equals(message.app)
                                                     && "ERROR".equals(message.result)
                                                     && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
