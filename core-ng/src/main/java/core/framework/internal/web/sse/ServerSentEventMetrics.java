package core.framework.internal.web.sse;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class ServerSentEventMetrics implements Metrics {
    public final List<ServerSentEventContextImpl<?>> contexts = new ArrayList<>();

    @Override
    public void collect(Stats stats) {
        int count = 0;
        for (ServerSentEventContextImpl<?> context : contexts) {
            count += context.size();
        }
        stats.put("sse_active_channels", count);
    }
}
