package core.framework.internal.web.sse;

import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

class ServerSentEventCloseHandler<T> implements ExchangeCompletionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSentEventCloseHandler.class);
    final ServerSentEventContextImpl<T> context;
    private final LogManager logManager;
    private final ChannelImpl<T> channel;
    private final Map<String, String> logContext;

    ServerSentEventCloseHandler(LogManager logManager, ChannelImpl<T> channel, ServerSentEventContextImpl<T> context, Map<String, String> logContext) {
        this.logManager = logManager;
        this.channel = channel;
        this.context = context;
        this.logContext = logContext;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
        exchange.dispatch(() -> {
            VirtualThread.COUNT.increase();
            ActionLog actionLog = logManager.begin("=== sse close begin ===", null);
            try {
                actionLog.action("sse:" + exchange.getRequestPath() + ":close");
                actionLog.context("channel", channel.id);
                LOGGER.debug("refId={}", channel.refId);
                List<String> refIds = List.of(channel.refId);
                actionLog.refIds = refIds;
                actionLog.correlationIds = refIds;

                for (Map.Entry<String, String> entry : logContext.entrySet()) {
                    actionLog.context(entry.getKey(), entry.getValue());
                }

                if (!channel.groups.isEmpty()) actionLog.context("group", channel.groups.toArray());
                context.remove(channel);
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
