package app.monitor.job;

import core.framework.http.HTTPClientException;
import core.framework.internal.stat.Stats;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ElasticSearchMonitorJobTest {
    @Mock
    ElasticSearchClient elasticSearchClient;
    @Mock
    MessagePublisher<StatMessage> publisher;

    private ElasticSearchMonitorJob job;

    @BeforeEach
    void createElasticSearchMonitorJob() {
        job = new ElasticSearchMonitorJob(elasticSearchClient, "es", "localhost", publisher);
    }

    @Test
    void execute() throws IOException {
        when(elasticSearchClient.stats("localhost"))
                .thenReturn(JSON.fromJSON(ElasticSearchNodeStats.class, ClasspathResources.text("es-job-test/stats.json")));
        job.execute(null);

        verify(publisher).publish(argThat(message -> "es-0".equals(message.host)));
    }

    @Test
    void collect() {
        ElasticSearchNodeStats nodeStats = JSON.fromJSON(ElasticSearchNodeStats.class, ClasspathResources.text("es-job-test/stats.json"));
        Stats stats = job.collect(nodeStats.nodes.get("lwOM4R6MQTOinxEVW1antA"));
        assertThat(stats.stats)
                .containsEntry("es_docs", 49029079d)
                .containsKeys("es_disk_used", "es_disk_max", "es_heap_used", "es_heap_max", "es_gc_young_count", "es_gc_old_elapsed");
    }

    @Test
    void publishError() throws IOException {
        when(elasticSearchClient.stats("localhost")).thenThrow(new HTTPClientException("mock", "MOCK_ERROR_CODE", null));
        job.execute(null);
        verify(publisher).publish(argThat(message -> "es".equals(message.app)
                && "ERROR".equals(message.result)
                && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
