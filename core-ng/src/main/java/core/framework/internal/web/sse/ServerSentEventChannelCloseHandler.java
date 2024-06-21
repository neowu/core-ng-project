package core.framework.internal.web.sse;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServerSentEventChannelCloseHandler<T> implements ExchangeCompletionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSentEventChannelCloseHandler.class);
    final ServerSentEventContextImpl<T> context;
    private final LogManager logManager;
    private final ServerSentEventChannelImpl<T> channel;

    public ServerSentEventChannelCloseHandler(LogManager logManager, ServerSentEventChannelImpl<T> channel, ServerSentEventContextImpl<T> context) {
        this.logManager = logManager;
        this.channel = channel;
        this.context = context;
    }

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
        ActionLog actionLog = logManager.begin("=== sse close begin ===", null);
        try {
            actionLog.action("sse:" + exchange.getRequestPath() + ":close");
            actionLog.context("channel", channel.id);
            LOGGER.debug("refId={}", channel.refId);
            List<String> refIds = List.of(channel.refId);
            actionLog.refIds = refIds;
            actionLog.correlationIds = refIds;
            if (!channel.groups.isEmpty()) actionLog.context("group", channel.groups.toArray());
            context.remove(channel);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            double duration = System.nanoTime() - channel.startTime;
            actionLog.stats.put("sse_duration", duration);
            logManager.end("=== sse close end ===");
            next.proceed();
        }
    }
}
