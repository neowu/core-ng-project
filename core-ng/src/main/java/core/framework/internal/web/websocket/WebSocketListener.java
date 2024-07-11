package core.framework.internal.web.websocket;

import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.http.RateControl;
import core.framework.util.Strings;
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
import java.util.List;

/**
 * @author neo
 */
final class WebSocketListener implements ChannelListener<WebSocketChannel> {
    private static final long MAX_TEXT_MESSAGE_SIZE = 10_000_000;     // limit max text message sent by client to 10M
    private final Logger logger = LoggerFactory.getLogger(WebSocketListener.class);

    private final LogManager logManager;
    private final RateControl rateControl;

    CloseListener closeListener = new CloseListener();

    WebSocketListener(LogManager logManager, RateControl rateControl) {
        this.logManager = logManager;
        this.rateControl = rateControl;
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl") // intentional
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
                        channel.getWorker().execute(() -> onFullTextMessage(channel, context, null));
                    }

                    @Override
                    public void onError(WebSocketChannel channel, BufferedTextMessage context, Throwable throwable) {
                        channel.getWorker().execute(() -> onFullTextMessage(channel, context, throwable));
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
                default -> throw new IOException("unexpected type, type=" + type);
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            IoUtils.safeClose(channel);
        }
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl") // intentional
    private void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage textMessage, Throwable error) {
        VirtualThread.COUNT.increase();     // refer to line 56, full text message is handled by getWorker()
        var wrapper = wrapper(channel);
        ActionLog actionLog = logManager.begin("=== ws message handling begin ===", null);
        try {
            actionLog.action("ws:" + wrapper.path);
            linkContext(channel, wrapper, actionLog);
            if (error != null) throw error;

            String data = textMessage.getData();
            logger.debug("[channel] message={}", data);     // not mask, assume ws message not containing sensitive info, the data can be json or plain text
            actionLog.track("ws", 0, data.length(), 0);

            validateRate(wrapper);

            Object message = wrapper.support.fromClientMessage(data);
            wrapper.support.listener.onMessage(wrapper, message);
        } catch (Throwable e) {
            logManager.logError(e);
            if (!channel.isCloseFrameSent()) {
                WebSockets.sendClose(WebSocketCloseCodes.closeCode(e), e.getMessage(), channel, ChannelCallback.INSTANCE);
            }
        } finally {
            logManager.end("=== ws message handling end ===");
            VirtualThread.COUNT.decrease();
        }
    }

    private void onFullCloseMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        final var wrapper = wrapper(channel);
        try (var data = message.getData()) {
            var closeMessage = new CloseMessage(data.getResource());
            wrapper.closeMessage = closeMessage;
            if (!channel.isCloseFrameSent()) {
                WebSockets.sendClose(closeMessage, channel, ChannelCallback.INSTANCE);
            }
        }
    }

    private void validateRate(ChannelImpl<?, ?> wrapper) {
        if (wrapper.support.limitRate != null) {
            rateControl.validateRate(wrapper.support.limitRate.value(), wrapper.clientIP);
        }
    }

    private void linkContext(WebSocketChannel channel, ChannelImpl<?, ?> wrapper, ActionLog actionLog) {
        actionLog.warningContext.maxProcessTimeInNano(WebSocketHandler.MAX_PROCESS_TIME_IN_NANO);
        actionLog.trace = wrapper.trace;
        actionLog.context("channel", wrapper.id);
        logger.debug("refId={}", wrapper.refId);
        List<String> refIds = List.of(wrapper.refId);
        actionLog.refIds = refIds;
        actionLog.correlationIds = refIds;
        logger.debug("[channel] url={}", channel.getUrl());
        logger.debug("[channel] remoteAddress={}", channel.getSourceAddress().getAddress().getHostAddress());
        actionLog.context("client_ip", wrapper.clientIP);
        actionLog.context("listener", wrapper.support.listener.getClass().getCanonicalName());
        if (!wrapper.groups.isEmpty()) actionLog.context("group", wrapper.groups.toArray());
    }

    @SuppressWarnings("unchecked")
    private ChannelImpl<Object, Object> wrapper(WebSocketChannel channel) {
        return (ChannelImpl<Object, Object>) channel.getAttribute(WebSocketHandler.CHANNEL_KEY);
    }

    class CloseListener implements ChannelListener<WebSocketChannel> {
        @Override
        public void handleEvent(WebSocketChannel channel) {
            channel.getWorker().execute(() -> {
                VirtualThread.COUNT.increase();
                var wrapper = wrapper(channel);
                ActionLog actionLog = logManager.begin("=== websocket close begin ===", null);
                try {
                    actionLog.action("ws:" + wrapper.path + ":close");
                    wrapper.support.context.remove(wrapper);    // context.remove() does not cleanup wrapper.groups, so can be logged below
                    linkContext(channel, wrapper, actionLog);

                    int code = wrapper.closeMessage == null ? WebSocketCloseCodes.ABNORMAL_CLOSURE : wrapper.closeMessage.getCode();
                    String reason = wrapper.closeMessage == null ? null : wrapper.closeMessage.getReason();
                    actionLog.context("close_code", code);

                    // close reason classified as text message, in theory it is not supposed to be put in context (which is only for keyword and searchable)
                    // but close action usually won't trigger warnings/trace, so we have to put it on context with restriction
                    if (reason != null && !reason.isEmpty()) actionLog.context("close_reason", Strings.truncate(reason, ActionLog.MAX_CONTEXT_VALUE_LENGTH));
                    actionLog.track("ws", 0, reason == null ? 0 : 1 + reason.length(), 0);   // size = code (1 int) + reason

                    wrapper.support.listener.onClose(wrapper, code, reason);
                } catch (Throwable e) {
                    logManager.logError(e);
                } finally {
                    double duration = System.nanoTime() - wrapper.startTime;
                    actionLog.stats.put("ws_duration", duration);
                    logManager.end("=== websocket close end ===");
                    VirtualThread.COUNT.decrease();
                }
            });
        }
    }

    private abstract class Callback<T> implements WebSocketCallback<T> {
        @Override
        public void onError(WebSocketChannel channel, T context, Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }
}
