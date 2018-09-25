package core.framework.impl.web.websocket;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
final class WebSocketMessageListener extends AbstractReceiveListener {
    private final Logger logger = LoggerFactory.getLogger(WebSocketMessageListener.class);
    private final LogManager logManager;

    WebSocketMessageListener(LogManager logManager) {
        this.logManager = logManager;
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        var wrapper = (ChannelImpl) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
        ActionLog actionLog = logManager.begin("=== ws message handling begin ===");
        try {
            actionLog.action(wrapper.action);
            actionLog.context("channel", wrapper.id);
            logger.debug("refId={}", wrapper.refId);
            List<String> refIds = List.of(wrapper.refId);
            actionLog.refIds = refIds;
            actionLog.correlationIds = refIds;
            logger.debug("[channel] url={}", channel.getUrl());
            logger.debug("[channel] remoteAddress={}", channel.getSourceAddress().getAddress().getHostAddress());
            actionLog.context("clientIP", wrapper.clientIP);
            String data = message.getData();
            logger.debug("[channel] message={}", data);
            actionLog.track("ws", 0, 1, 0);
            actionLog.context("listener", wrapper.listener.getClass().getCanonicalName());
            wrapper.listener.onMessage(wrapper, data);
        } catch (Throwable e) {
            logManager.logError(e);
            WebSockets.sendClose(CloseMessage.UNEXPECTED_ERROR, e.getMessage(), channel, ChannelCallback.INSTANCE);
        } finally {
            logManager.end("=== ws message handling end ===");
        }
    }
}
