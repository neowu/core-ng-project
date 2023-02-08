package core.framework.internal.web.websocket;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;

/**
 * @author neo
 */
public class WebSocketMetrics implements Metrics {
    private final WebSocketHandler handler;

    public WebSocketMetrics(WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void collect(Stats stats) {
        stats.put("ws_active_channels", handler.channels.size());
    }
}
