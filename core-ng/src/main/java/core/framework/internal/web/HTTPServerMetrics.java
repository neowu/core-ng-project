package core.framework.internal.web;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;
import org.xnio.management.XnioWorkerMXBean;

/**
 * @author neo
 */
public class HTTPServerMetrics implements Metrics {
    private final HTTPServer server;

    public HTTPServerMetrics(HTTPServer server) {
        this.server = server;
    }

    @Override
    public void collect(Stats stats) {
        XnioWorkerMXBean mxBean = server.mxBean();  // collect stats tasks start after http server and stop before http server, so mxBean() will not return null
        stats.put("http_queue_size", mxBean.getWorkerQueueSize());
        stats.put("http_busy_thread_count", mxBean.getBusyWorkerThreadCount());
    }
}
