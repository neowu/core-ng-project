package core.framework.impl.web;

import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xnio.management.XnioWorkerMXBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class HTTPServerMetricsTest {
    private HTTPServerMetrics metrics;
    private HTTPServer server;

    @BeforeEach
    void createHTTPServerMetrics() {
        server = mock(HTTPServer.class);
        metrics = new HTTPServerMetrics(server);
    }

    @Test
    void collect() {
        var mxBean = mock(XnioWorkerMXBean.class);
        when(mxBean.getBusyWorkerThreadCount()).thenReturn(2);
        when(mxBean.getWorkerQueueSize()).thenReturn(10);
        when(server.mxBean()).thenReturn(mxBean);

        Map<String, Double> stats = Maps.newHashMap();
        metrics.collect(stats);

        assertThat(stats)
            .containsEntry("http_queue_size", 10.0)
            .containsEntry("http_busy_thread_count", 2.0);
    }
}
