package app.monitor.job;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPResponse;
import core.framework.internal.stat.Stats;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ElasticSearchCollectorTest {
    private ElasticSearchCollector collector;
    private HTTPClient httpClient;

    @BeforeEach
    void createElasticSearchCollector() {
        httpClient = mock(HTTPClient.class);
        collector = new ElasticSearchCollector(httpClient, "localhost");
    }

    @Test
    void collect() {
        when(httpClient.execute(any()))
                .thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes("[{\"cpu\":\"1\",\"disk.used\":\"6079250432\",\"disk.total\":\"62725623808\",\"heap.current\":\"139664376\",\"heap.max\":\"519438336\",\"ram.current\":\"1444081664\",\"ram.max\":\"2086522880\"}]")))
                .thenReturn(new HTTPResponse(200, Map.of(), Strings.bytes("[{\"count\":\"1\"}]")));

        Stats stats = collector.collect();

        assertThat(stats.stats)
                .containsEntry("es_cpu_usage", 0.01)
                .containsEntry("es_docs", 1d)
                .containsKeys("es_mem_used", "es_mem_max");
    }
}
