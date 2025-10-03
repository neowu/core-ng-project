package core.framework.internal.web.sse;

import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

import java.util.List;

class ServerSentEventCloseHandler<T> implements ExchangeCompletionListener {
    private final LogManager logManager;
    private final ChannelImpl<T> channel;
    private final ChannelSupport<T> support;

    ServerSentEventCloseHandler(LogManager logManager, ChannelImpl<T> channel, ChannelSupport<T> support) {
        this.logManager = logManager;
        this.channel = channel;
        this.support = support;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
        exchange.dispatch(() -> {
            VirtualThread.COUNT.increase();
            try {
                logManager.run("sse", null, this::close);
            } finally {
                VirtualThread.COUNT.decrease();
            }
        });
        next.proceed();
    }

    private Void close(ActionLog actionLog) {
        try {
            actionLog.action("sse:" + channel.path + ":close");
            actionLog.context("channel", channel.id);
            List<String> refIds = List.of(channel.refId);
            actionLog.refIds = refIds;
            actionLog.correlationIds = refIds;

            actionLog.context("client_ip", channel.clientIP);
            if (channel.traceId != null) actionLog.context("trace_id", channel.traceId);

            if (!channel.groups.isEmpty()) actionLog.context("group", channel.groups.toArray());
            support.context.remove(channel);
            // at this point (as ExchangeCompleteListener), exchange is closed, no need to close channel again

            actionLog.stats.put("sse_event_count", (double) channel.eventCount);
            actionLog.stats.put("sse_event_size", (double) channel.eventSize);

            actionLog.context("listener", support.listener.getClass().getCanonicalName());
            support.listener.onClose(channel);  // run onClose at last in case it throws exception to break flow
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            double duration = System.nanoTime() - channel.startTime;
            actionLog.stats.put("sse_duration", duration);
        }
        return null;
    }
}
