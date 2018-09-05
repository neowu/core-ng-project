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
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.spi.AsyncWebSocketHttpServerExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class WebSocketHandler implements org.xnio.ChannelListener<WebSocketChannel> {
    static final String CHANNEL_KEY = "CHANNEL";
    public final Map<String, ChannelListener> listeners = new HashMap<>();
    public final WebSocketContextImpl context = new WebSocketContextImpl();
    private final Set<WebSocketChannel> channels = Sets.newConcurrentHashSet();
    private final Handshake handshake = new Hybi13Handshake();
    private final WebSocketMessageListener messageListener;

    public WebSocketHandler(LogManager logManager) {
        messageListener = new WebSocketMessageListener(logManager);
    }

    public boolean isWebSocket(HTTPMethod method, HeaderMap headers) {
        return method == HTTPMethod.GET
                && headers.getFirst(Headers.SEC_WEB_SOCKET_KEY) != null
                && headers.getFirst(Headers.SEC_WEB_SOCKET_VERSION).equals("13");  // only support latest ws version
    }

    // refer to io.undertow.websockets.WebSocketProtocolHandshakeHandler
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
            var wrapper = new ChannelImpl(channel, context, listener);
            wrapper.action = action;
            wrapper.clientIP = request.clientIP();
            wrapper.refId = actionLog.id;
            actionLog.context("channel", wrapper.id);
            channel.setAttribute(CHANNEL_KEY, wrapper);
            channel.addCloseTask(this);

            listener.onConnect(request, wrapper);
            channel.getReceiveSetter().set(messageListener);
            channel.resumeReceives();
        });
        handshake.handshake(webSocketExchange);
    }

    public void shutdown() {
        for (var channel : channels) {
            WebSockets.sendClose(CloseMessage.GOING_AWAY, "server is shutting down", channel, ChannelCallback.INSTANCE);
        }
    }

    @Override
    public void handleEvent(WebSocketChannel channel) { // only handle channel close event
        var wrapper = (ChannelImpl) channel.getAttribute(CHANNEL_KEY);
        context.remove(wrapper);
    }
}
