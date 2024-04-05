package core.framework.internal.web.websocket;

import core.framework.http.HTTPMethod;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.http.RateControl;
import core.framework.internal.web.request.RequestImpl;
import core.framework.internal.web.session.SessionManager;
import core.framework.util.Sets;
import core.framework.web.Session;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.NotFoundException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.spi.AsyncWebSocketHttpServerExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class WebSocketHandler {
    static final String CHANNEL_KEY = "CHANNEL";
    public final WebSocketContextImpl context = new WebSocketContextImpl();

    // passes to AsyncWebSocketHttpServerExchange as peerConnections, channel will remove self on close
    // refer to io.undertow.websockets.core.WebSocketChannel.WebSocketChannel
    final Set<WebSocketChannel> channels = Sets.newConcurrentHashSet();

    private final Handshake handshake = new Hybi13Handshake();
    private final Map<String, ChannelHandler<?, ?>> handlers = new HashMap<>();

    private final WebSocketListener listener;
    private final SessionManager sessionManager;
    private final LogManager logManager;

    public WebSocketHandler(LogManager logManager, SessionManager sessionManager, RateControl rateControl) {
        this.logManager = logManager;
        this.sessionManager = sessionManager;
        listener = new WebSocketListener(logManager, context, rateControl);
    }

    public boolean checkWebSocket(HTTPMethod method, HeaderMap headers) {
        if (method == HTTPMethod.GET && headers.getFirst(Headers.SEC_WEB_SOCKET_KEY) != null) {
            if (!headers.contains(Headers.UPGRADE)) {
                throw new BadRequestException("upgrade is not permitted", "INVALID_HTTP_REQUEST");
            }

            String version = headers.getFirst(Headers.SEC_WEB_SOCKET_VERSION);
            if ("13".equals(version)) return true;  // only support latest ws version
            throw new BadRequestException("only support web socket version 13, version=" + version, "INVALID_HTTP_REQUEST");
        }
        return false;
    }

    // refer to io.undertow.websockets.WebSocketProtocolHandshakeHandler
    public void handle(HttpServerExchange exchange, RequestImpl request, ActionLog actionLog) {
        String path = exchange.getRequestPath();
        String action = "ws:" + path;
        actionLog.action(action + ":open");

        @SuppressWarnings("unchecked")
        ChannelHandler<Object, Object> handler = (ChannelHandler<Object, Object>) handlers.get(path);
        if (handler == null) throw new NotFoundException("not found, path=" + path, "PATH_NOT_FOUND");

        request.session = loadSession(request, actionLog);  // load session as late as possible, so for sniffer/scan request with sessionId, it won't call redis every time even for 404/405

        var webSocketExchange = new AsyncWebSocketHttpServerExchange(exchange, channels);
        exchange.upgradeChannel((connection, httpServerExchange) -> {
            WebSocketChannel channel = handshake.createChannel(webSocketExchange, connection, webSocketExchange.getBufferPool());
            // not set idle timeout for channel, e.g. channel.setIdleTimeout(timeout);
            // in cloud env, timeout is set on LB (azure AG, or gcloud LB), usually use 300s timeout
            try {
                var wrapper = new ChannelImpl<>(channel, context, handler);
                wrapper.action = action;
                wrapper.clientIP = request.clientIP();
                wrapper.refId = actionLog.id;   // with ws, correlationId and refId are same as parent http action id
                wrapper.trace = actionLog.trace;
                actionLog.context("channel", wrapper.id);

                channel.setAttribute(CHANNEL_KEY, wrapper);
                channel.addCloseTask(listener.closeListener);
                context.add(wrapper);

                channel.getReceiveSetter().set(listener);
                channel.resumeReceives();

                channels.add(channel);

                handler.listener.onConnect(request, wrapper);
                actionLog.context("room", wrapper.rooms.toArray()); // may join room onConnect
            } catch (Throwable e) {
                // upgrade is handled by io.undertow.server.protocol.http.HttpReadListener.exchangeComplete, and it catches all exceptions during onConnect
                logManager.logError(e);
                WebSockets.sendClose(WebSocketCloseCodes.closeCode(e), e.getMessage(), channel, ChannelCallback.INSTANCE);
            }
        });
        handshake.handshake(webSocketExchange);
    }

    Session loadSession(RequestImpl request, ActionLog actionLog) {
        Session session = sessionManager.load(request, actionLog);
        if (session == null) return null;
        return new ReadOnlySession(session);
    }

    public void shutdown() {
        for (WebSocketChannel channel : channels) {
            WebSockets.sendClose(WebSocketCloseCodes.SERVICE_RESTART, "server is shutting down", channel, ChannelCallback.INSTANCE);
        }
    }

    public void add(String path, ChannelHandler<?, ?> handler) {
        ChannelHandler<?, ?> previous = handlers.putIfAbsent(path, handler);
        if (previous != null) throw new Error(format("found duplicate channel listener, path={}, previousListener={}", path, previous.listener.getClass().getCanonicalName()));
    }
}
