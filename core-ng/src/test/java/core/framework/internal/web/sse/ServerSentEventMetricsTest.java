package core.framework.internal.web.sse;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerSentEventMetricsTest {
    private ServerSentEventMetrics metrics;

    @BeforeEach
    void createServerSentEventMetrics() {
        metrics = new ServerSentEventMetrics();
    }

    @Test
    void collect() {
        ServerSentEventContextImpl<Object> context = new ServerSentEventContextImpl<>();
        context.add(new ChannelImpl<>(null, null, null, null));
        metrics.contexts.add(context);

        var stats = new Stats();
        metrics.collect(stats);
        assertThat(stats.stats)
            .containsEntry("sse_active_channels", 1.0d);
    }
}
