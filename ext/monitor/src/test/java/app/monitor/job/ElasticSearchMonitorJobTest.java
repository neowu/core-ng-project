package app.monitor.job;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientException;
import core.framework.http.HTTPResponse;
import core.framework.internal.stat.Stats;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.test.MockitoExtension;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ElasticSearchMonitorJobTest {
    @Mock
    HTTPClient httpClient;
    @Mock
    MessagePublisher<StatMessage> publisher;

    private ElasticSearchMonitorJob job;

    @BeforeEach
    void createElasticSearchMonitorJob() {
        job = new ElasticSearchMonitorJob(httpClient, "es", "localhost", publisher);
    }

    @Test
    void collectCount() {
        when(httpClient.execute(any()))
                .thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes("[{\"count\":\"1\"}]")));

        double count = job.collectCount();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void collect() {
        Stats stats = job.collect(Map.of("disk.used", "6079250432",
                "disk.total", "62725623808",
                "heap.current", "139664376",
                "heap.max", "519438336"), 1);

        assertThat(stats.stats)
                .containsEntry("es_docs", 1d)
                .containsKeys("es_disk_used", "es_disk_max", "es_heap_used", "es_heap_max");
    }

    @Test
    void publishError() {
        when(httpClient.execute(any())).thenThrow(new HTTPClientException("mock", "MOCK_ERROR_CODE", null));
        job.execute(null);
        verify(publisher).publish(argThat(message -> "es".equals(message.app)
                && "ERROR".equals(message.result)
                && "FAILED_TO_COLLECT".equals(message.errorCode)));
    }
}
