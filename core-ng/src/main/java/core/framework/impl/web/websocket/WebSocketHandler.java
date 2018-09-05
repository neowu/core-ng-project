package core.framework.impl.web.websocket;

import core.framework.http.HTTPMethod;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.request.RequestImpl;
import core.framework.util.Sets;
import core.framework.web.exception.NotFoundException;
import core.framework.web.websocket.ChannelListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.spi.AsyncWebSocketHttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class WebSocketHandler {
    private static final String CHANNEL_KEY = "CHANNEL";
    public final Map<String, ChannelListener> listeners = new HashMap<>();
    private final Set<WebSocketChannel> channels = Sets.newConcurrentHashSet();
    private final Handshake handshake = new Hybi13Handshake();
    private final WebSocketListener webSocketListener;

    public WebSocketHandler(LogManager logManager) {
        webSocketListener = new WebSocketListener(logManager);
    }

    public boolean isWebSocket(HTTPMethod method, HeaderMap headers) {
        return method == HTTPMethod.GET
                && headers.getFirst(Headers.SEC_WEB_SOCKET_KEY) != null
                && headers.getFirst(Headers.SEC_WEB_SOCKET_VERSION).equals("13");  // only support latest ws version
    }

    public void handle(HttpServerExchange exchange, RequestImpl request, ActionLog actionLog) {
        String path = exchange.getRequestPath();
        String action = "ws:" + path;
        actionLog.action(action);

        ChannelListener listener = listeners.get(path);
        if (listener == null) throw new NotFoundException("not found, path=" + path, "PATH_NOT_FOUND");

        var webSocketExchange = new AsyncWebSocketHttpServerExchange(exchange, channels);
        exchange.upgradeChannel((connection, httpServerExchange) -> {
            WebSocketChannel channel = handshake.createChannel(webSocketExchange, connection, webSocketExchange.getBufferPool());
            channels.add(channel);

            var wrapper = new ChannelImpl(channel, action, request.clientIP(), actionLog.id, listener);
            channel.setAttribute(CHANNEL_KEY, wrapper);

            listener.onConnect(request, wrapper);
            channel.getReceiveSetter().set(webSocketListener);
            channel.resumeReceives();
        });
        handshake.handshake(webSocketExchange);
    }

    public void shutdown() {
        for (var channel : channels) {
            WebSockets.sendClose(CloseMessage.GOING_AWAY, "server is shutting down", channel, null);
        }
    }

    private static class WebSocketListener extends AbstractReceiveListener {
        private final Logger logger = LoggerFactory.getLogger(WebSocketListener.class);
        private final LogManager logManager;

        private WebSocketListener(LogManager logManager) {
            this.logManager = logManager;
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
            var wrapper = (ChannelImpl) channel.getAttribute(CHANNEL_KEY);
            ActionLog actionLog = logManager.begin("=== ws message handling begin ===");
            try {
                actionLog.action(wrapper.action);
                actionLog.refId(wrapper.refId);
                logger.debug("[channel] url={}", channel.getUrl());
                actionLog.context("listener", wrapper.listener.getClass().getCanonicalName());
                logger.debug("[channel] remoteAddress={}", channel.getSourceAddress().getAddress().getHostAddress());
                actionLog.context("clientIP", wrapper.clientIP);
                String data = message.getData();
                logger.debug("[channel] message={}", data);
                actionLog.track("ws", 0, 1, 0);
                wrapper.listener.onMessage(wrapper, data);
            } catch (Throwable e) {
                logManager.logError(e);
                WebSockets.sendClose(CloseMessage.UNEXPECTED_ERROR, e.getMessage(), channel, null);
            } finally {
                logManager.end("=== ws message handling end ===");
            }
        }

        @Override
        protected void onCloseMessage(CloseMessage message, WebSocketChannel channel) {
            var wrapper = (ChannelImpl) channel.getAttribute(CHANNEL_KEY);
            wrapper.listener.onClose(wrapper);
        }
    }
}
