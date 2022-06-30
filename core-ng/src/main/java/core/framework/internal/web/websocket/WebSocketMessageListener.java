package core.framework.internal.web.websocket;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.http.RateControl;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.TooManyRequestsException;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
final class WebSocketMessageListener extends AbstractReceiveListener {
    private static final long MAX_TEXT_MESSAGE_SIZE = 10_000_000;     // limit max text message sent by client to 10M
    private final long maxProcessTimeInNano = Duration.ofSeconds(300).toNanos();    // generally we set 300s on LB for websocket timeout
    private final Logger logger = LoggerFactory.getLogger(WebSocketMessageListener.class);
    private final LogManager logManager;
    private final RateControl rateControl;

    WebSocketMessageListener(LogManager logManager, RateControl rateControl) {
        this.logManager = logManager;
        this.rateControl = rateControl;
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage textMessage) {
        @SuppressWarnings("unchecked")
        var wrapper = (ChannelImpl<Object, Object>) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
        ActionLog actionLog = logManager.begin("=== ws message handling begin ===", null);
        try {
            actionLog.action(wrapper.action);
            linkContext(channel, wrapper, actionLog);

            String data = textMessage.getData();
            logger.debug("[channel] message={}", data);     // not mask, assume ws message not containing sensitive info, the data can be json or plain text
            actionLog.track("ws", 0, 1, 0, null);

            validateRate(wrapper);

            Object message = wrapper.handler.fromClientMessage(data);
            wrapper.handler.listener.onMessage(wrapper, message);
        } catch (Throwable e) {
            logManager.logError(e);
            WebSockets.sendClose(closeCode(e), e.getMessage(), channel, ChannelCallback.INSTANCE);
        } finally {
            logManager.end("=== ws message handling end ===");
        }
    }

    private void validateRate(ChannelImpl<?, ?> wrapper) {
        if (rateControl.config != null && wrapper.handler.limitRate != null) {
            rateControl.validateRate(wrapper.handler.limitRate.value(), wrapper.clientIP);
        }
    }

    @Override
    protected void onCloseMessage(CloseMessage message, WebSocketChannel channel) {
        @SuppressWarnings("unchecked")
        var wrapper = (ChannelImpl<Object, Object>) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
        ActionLog actionLog = logManager.begin("=== ws close message handling begin ===", null);
        try {
            actionLog.action(wrapper.action + ":close");
            linkContext(channel, wrapper, actionLog);

            int code = message.getCode();
            String reason = message.getReason();
            actionLog.context("code", code);
            logger.debug("[channel] reason={}", reason);
            actionLog.track("ws", 0, 1, 0, null);

            wrapper.handler.listener.onClose(wrapper, code, reason);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            logManager.end("=== ws close message handling end ===");
        }
    }

    private void linkContext(WebSocketChannel channel, ChannelImpl<?, ?> wrapper, ActionLog actionLog) {
        actionLog.warningContext.maxProcessTimeInNano(maxProcessTimeInNano);
        actionLog.context("channel", wrapper.id);
        logger.debug("refId={}", wrapper.refId);
        List<String> refIds = List.of(wrapper.refId);
        actionLog.refIds = refIds;
        actionLog.correlationIds = refIds;
        logger.debug("[channel] url={}", channel.getUrl());
        logger.debug("[channel] remoteAddress={}", channel.getSourceAddress().getAddress().getHostAddress());
        actionLog.context("client_ip", wrapper.clientIP);
        actionLog.context("listener", wrapper.handler.listener.getClass().getCanonicalName());
        actionLog.context("room", wrapper.rooms.toArray());
    }

    // as websocket does not have restful convention, here only supports general cases
    int closeCode(Throwable e) {
        if (e instanceof TooManyRequestsException) return WebSocketCloseCodes.TRY_AGAIN_LATER;
        if (e instanceof BadRequestException) return WebSocketCloseCodes.POLICY_VIOLATION;
        return WebSocketCloseCodes.INTERNAL_ERROR;
    }

    // disable binary message completely
    @Override
    protected void onBinary(WebSocketChannel webSocketChannel, StreamSourceFrameChannel messageChannel) {
        WebSockets.sendClose(new CloseMessage(CloseMessage.PROTOCOL_ERROR, "binary message is not supported"), webSocketChannel, null);
    }

    @Override
    protected long getMaxTextBufferSize() {
        return MAX_TEXT_MESSAGE_SIZE;
    }

    // refer to https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers#pings_and_pongs_the_heartbeat_of_websockets
    @Override
    protected long getMaxPingBufferSize() {
        return 125;
    }

    @Override
    protected long getMaxPongBufferSize() {
        return 125;
    }

    // refer to https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close
    @Override
    protected long getMaxCloseBufferSize() {
        return 123;
    }

    // log errors on reading incoming messages/ping/pong/close, e.g. message size is too large
    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
        super.onError(channel, error);
        logger.warn(error.getMessage(), error);
    }
}
