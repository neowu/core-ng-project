package core.framework.impl.web;

import core.framework.internal.stat.Metrics;
import org.xnio.management.XnioWorkerMXBean;

import java.util.Map;

/**
 * @author neo
 */
public class HTTPServerMetrics implements Metrics {
    private final HTTPServer server;

    public HTTPServerMetrics(HTTPServer server) {
        this.server = server;
    }

    @Override
    public void collect(Map<String, Double> stats) {
        XnioWorkerMXBean mxBean = server.mxBean();  // collect stats tasks start after http server and stop before http server, so mxBean() will not return null
        stats.put("http_queue_size", (double) mxBean.getWorkerQueueSize());
        stats.put("http_busy_thread_count", (double) mxBean.getBusyWorkerThreadCount());
    }
}
