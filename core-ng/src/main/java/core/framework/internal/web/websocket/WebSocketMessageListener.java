package core.framework.internal.web.websocket;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
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
            linkContext(channel, wrapper, actionLog);

            String data = message.getData();
            logger.debug("[channel] message={}", data);
            actionLog.track("ws", 0, 1, 0);

            wrapper.listener.onMessage(wrapper, data);
        } catch (Throwable e) {
            logManager.logError(e);
            WebSockets.sendClose(CloseMessage.UNEXPECTED_ERROR, e.getMessage(), channel, ChannelCallback.INSTANCE);
        } finally {
            logManager.end("=== ws message handling end ===");
        }
    }

    @Override
    protected void onCloseMessage(CloseMessage message, WebSocketChannel channel) {
        var wrapper = (ChannelImpl) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
        ActionLog actionLog = logManager.begin("=== ws close message handling begin ===");
        try {
            actionLog.action(wrapper.action + ":close");
            linkContext(channel, wrapper, actionLog);

            int code = message.getCode();
            String reason = message.getReason();
            actionLog.context("code", code);
            logger.debug("[channel] reason={}", reason);
            actionLog.track("ws", 0, 1, 0);

            wrapper.listener.onClose(wrapper, code, reason);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            logManager.end("=== ws close message handling end ===");
        }
    }

    private void linkContext(WebSocketChannel channel, ChannelImpl wrapper, ActionLog actionLog) {
        actionLog.context("channel", wrapper.id);
        logger.debug("refId={}", wrapper.refId);
        List<String> refIds = List.of(wrapper.refId);
        actionLog.refIds = refIds;
        actionLog.correlationIds = refIds;
        logger.debug("[channel] url={}", channel.getUrl());
        logger.debug("[channel] remoteAddress={}", channel.getSourceAddress().getAddress().getHostAddress());
        actionLog.context("client_ip", wrapper.clientIP);
        actionLog.context("listener", wrapper.listener.getClass().getCanonicalName());
        actionLog.context("room", wrapper.rooms.toArray());
    }
}
