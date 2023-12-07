package core.framework.internal.web;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;

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
        int activeRequests = server.shutdownHandler.activeRequests.max();
        stats.put("http_active_requests", activeRequests);
    }
}
