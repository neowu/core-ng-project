package core.framework.internal.web.websocket;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.http.RateControl;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.TooManyRequestsException;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketFrameType;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
final class WebSocketMessageListener implements ChannelListener<WebSocketChannel> {
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
    public void handleEvent(WebSocketChannel channel) {
        try {
            StreamSourceFrameChannel result = channel.receive();
            if (result == null) return;
            WebSocketFrameType type = result.getType();

            switch (type) {
                case BINARY -> WebSockets.sendClose(CloseMessage.PROTOCOL_ERROR, "binary message is not supported", channel, null);
                case TEXT -> new BufferedTextMessage(MAX_TEXT_MESSAGE_SIZE, true).read(result, new WebSocketCallback<>() {
                    @Override
                    public void complete(WebSocketChannel channel, BufferedTextMessage context) {
                        onFullTextMessage(channel, context, null);
                    }

                    @Override
                    public void onError(WebSocketChannel channel, BufferedTextMessage context, Throwable throwable) {
                        onFullTextMessage(channel, context, throwable);
                    }
                });
                case PING -> new BufferedBinaryMessage(125, true).read(result, new Callback<>() {
                    @Override
                    public void complete(WebSocketChannel channel, BufferedBinaryMessage message) {
                        WebSockets.sendPong(message.getData().getResource(), channel, ChannelCallback.INSTANCE);
                        message.getData().free();
                    }
                });
                case PONG -> new BufferedBinaryMessage(125, true).read(result, new Callback<>() {
                    @Override
                    public void complete(WebSocketChannel channel, BufferedBinaryMessage message) {
                        message.getData().free();
                    }
                });
                // refer to https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close
                case CLOSE -> new BufferedBinaryMessage(125, true).read(result, new Callback<>() {
                    @Override
                    public void complete(WebSocketChannel channel, BufferedBinaryMessage context) {
                        onFullCloseMessage(channel, context);
                    }
                });
                default -> throw new Error("unexpected type, type=" + type);
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            IoUtils.safeClose(channel);
        }
    }

    private void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage textMessage, Throwable error) {
        @SuppressWarnings("unchecked")
        var wrapper = (ChannelImpl<Object, Object>) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
        ActionLog actionLog = logManager.begin("=== ws message handling begin ===", null);
        try {
            actionLog.action(wrapper.action);
            linkContext(channel, wrapper, actionLog);
            if (error != null) throw error;

            String data = textMessage.getData();
            logger.debug("[channel] message={}", data);     // not mask, assume ws message not containing sensitive info, the data can be json or plain text
            actionLog.track("ws", 0, 1, 0);

            validateRate(wrapper);

            Object message = wrapper.handler.fromClientMessage(data);
            wrapper.handler.listener.onMessage(wrapper, message);
        } catch (Throwable e) {
            logManager.logError(e);
            if (!channel.isCloseFrameSent()) {
                WebSockets.sendClose(closeCode(e), e.getMessage(), channel, ChannelCallback.INSTANCE);
            }
        } finally {
            logManager.end("=== ws message handling end ===");
        }
    }

    private void onFullCloseMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        @SuppressWarnings("unchecked")
        var wrapper = (ChannelImpl<Object, Object>) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
        ActionLog actionLog = logManager.begin("=== ws close message handling begin ===", null);

        try (var data = message.getData()) {
            actionLog.action(wrapper.action + ":close");
            linkContext(channel, wrapper, actionLog);
            var closeMessage = new CloseMessage(data.getResource());

            int code = closeMessage.getCode();
            String reason = closeMessage.getReason();
            actionLog.context("code", code);
            logger.debug("[channel] reason={}", reason);
            actionLog.track("ws", 0, 1, 0);

            wrapper.handler.listener.onClose(wrapper, code, reason);

            if (!channel.isCloseFrameSent()) {
                WebSockets.sendClose(closeMessage, channel, null);
            }
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            logManager.end("=== ws close message handling end ===");
        }
    }

    private void validateRate(ChannelImpl<?, ?> wrapper) {
        if (rateControl.config != null && wrapper.handler.limitRate != null) {
            rateControl.validateRate(wrapper.handler.limitRate.value(), wrapper.clientIP);
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

    private abstract class Callback<T> implements WebSocketCallback<T> {
        @Override
        public void onError(WebSocketChannel channel, T context, Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }
}
