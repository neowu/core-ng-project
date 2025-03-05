package core.framework.internal.web.sse;

import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

import java.util.List;

class ServerSentEventCloseHandler<T> implements ExchangeCompletionListener {
    final ChannelSupport<T> support;
    private final LogManager logManager;
    private final ChannelImpl<T> channel;

    ServerSentEventCloseHandler(LogManager logManager, ChannelImpl<T> channel, ChannelSupport<T> support) {
        this.logManager = logManager;
        this.channel = channel;
        this.support = support;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
        exchange.dispatch(() -> {
            VirtualThread.COUNT.increase();
            ActionLog actionLog = logManager.begin("=== sse close begin ===", null);
            try {
                actionLog.action("sse:" + exchange.getRequestPath() + ":close");
                actionLog.context("channel", channel.id);
                List<String> refIds = List.of(channel.refId);
                actionLog.refIds = refIds;
                actionLog.correlationIds = refIds;

                actionLog.context("client_id", channel.clientIP);
                if (channel.traceId != null) actionLog.context("trace_id", channel.traceId);

                support.listener.onClose(channel);

                if (!channel.groups.isEmpty()) actionLog.context("group", channel.groups.toArray());
                support.context.remove(channel);
                channel.shutdown();
            } catch (Throwable e) {
                logManager.logError(e);
            } finally {
                double duration = System.nanoTime() - channel.startTime;
                actionLog.stats.put("sse_duration", duration);
                logManager.end("=== sse close end ===");
                VirtualThread.COUNT.decrease();
            }
        });
        next.proceed();
    }
}
