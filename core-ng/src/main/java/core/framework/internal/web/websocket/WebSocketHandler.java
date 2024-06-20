package core.framework.internal.web.websocket;

import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.HTTPHandlerContext;
import core.framework.internal.web.request.RequestImpl;
import core.framework.internal.web.session.ReadOnlySession;
import core.framework.internal.web.session.SessionManager;
import core.framework.module.WebSocketConfig;
import core.framework.util.Sets;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.NotFoundException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
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

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class WebSocketHandler implements HttpHandler {
    static final String CHANNEL_KEY = "CHANNEL";

    // passes to AsyncWebSocketHttpServerExchange as peerConnections, channel will remove self on close
    // refer to io.undertow.websockets.core.WebSocketChannel.WebSocketChannel
    final Set<WebSocketChannel> channels = Sets.newConcurrentHashSet();

    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final Handshake handshake = new Hybi13Handshake();
    private final Map<String, ChannelHandler<?, ?>> handlers = new HashMap<>();

    private final LogManager logManager;
    private final WebSocketListener listener;
    private final SessionManager sessionManager;
    private final HTTPHandlerContext handlerContext;

    public WebSocketHandler(LogManager logManager, SessionManager sessionManager, HTTPHandlerContext handlerContext) {
        this.logManager = logManager;
        this.sessionManager = sessionManager;
        this.handlerContext = handlerContext;
        listener = new WebSocketListener(logManager, handlerContext.rateControl);
    }

    public boolean check(HttpString method, HeaderMap headers) {
        return Methods.GET.equals(method) && headers.getFirst(Headers.SEC_WEB_SOCKET_KEY) != null;
    }

    // refer to io.undertow.websockets.WebSocketProtocolHandshakeHandler
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    @Override
    public void handleRequest(HttpServerExchange exchange) {
        VirtualThread.COUNT.increase();
        long httpDelay = System.nanoTime() - exchange.getRequestStartTime();
        ActionLog actionLog = logManager.begin("=== websocket upgrade begin ===", null);
        var request = new RequestImpl(exchange, handlerContext.requestBeanReader);
        try {
            logger.debug("httpDelay={}", httpDelay);
            actionLog.stats.put("http_delay", (double) httpDelay);
            handlerContext.requestParser.parse(request, exchange, actionLog);

            validateWebSocketHeaders(exchange.getRequestHeaders());
            if (handlerContext.accessControl != null) handlerContext.accessControl.validate(request.clientIP());

            String path = request.path();
            @SuppressWarnings("unchecked")
            ChannelHandler<Object, Object> handler = (ChannelHandler<Object, Object>) handlers.get(path);
            if (handler == null) throw new NotFoundException("not found, path=" + path, "PATH_NOT_FOUND");

            actionLog.action("ws:" + path + ":open");

            handlerContext.rateControl.validateRate(WebSocketConfig.WS_OPEN_GROUP, request.clientIP());

            request.session = ReadOnlySession.of(sessionManager.load(request, actionLog));  // load session as late as possible, so for sniffer/scan request with sessionId, it won't call redis every time even for 404/405

            upgrade(exchange, request, handler, actionLog);
        } catch (Throwable e) {
            logManager.logError(e);
            exchange.endExchange();
        } finally {
            logManager.end("=== websocket upgrade end ===");
            VirtualThread.COUNT.decrease();
        }
    }

    private void upgrade(HttpServerExchange exchange, RequestImpl request, ChannelHandler<Object, Object> handler, ActionLog actionLog) {
        var webSocketExchange = new AsyncWebSocketHttpServerExchange(exchange, channels);
        exchange.upgradeChannel((connection, httpServerExchange) -> {
            WebSocketChannel channel = handshake.createChannel(webSocketExchange, connection, webSocketExchange.getBufferPool());
            // not set idle timeout for channel, e.g. channel.setIdleTimeout(timeout);
            // in cloud env, timeout is set on LB (azure AG, or gcloud LB), usually use 300s timeout
            try {
                var wrapper = new ChannelImpl<>(channel, handler);
                wrapper.path = request.path();
                wrapper.clientIP = request.clientIP();
                wrapper.refId = actionLog.id;   // with ws, correlationId and refId are same as parent http action id
                wrapper.trace = actionLog.trace;
                actionLog.context("channel", wrapper.id);

                channel.setAttribute(CHANNEL_KEY, wrapper);
                channel.addCloseTask(listener.closeListener);
                handler.webSocketContext.add(wrapper);

                channel.getReceiveSetter().set(listener);
                channel.resumeReceives();

                channels.add(channel);

                handler.listener.onConnect(request, wrapper);
                if (!wrapper.groups.isEmpty()) actionLog.context("group", wrapper.groups.toArray()); // may join room onConnect
            } catch (Throwable e) {
                // upgrade is handled by io.undertow.server.protocol.http.HttpReadListener.exchangeComplete, and it catches all exceptions during onConnect
                logManager.logError(e);
                WebSockets.sendClose(WebSocketCloseCodes.closeCode(e), e.getMessage(), channel, ChannelCallback.INSTANCE);
            }
        });
        handshake.handshake(webSocketExchange);
    }

    void validateWebSocketHeaders(HeaderMap headers) {
        if (!headers.contains(Headers.UPGRADE)) {
            throw new BadRequestException("upgrade is not permitted", "INVALID_HTTP_REQUEST");
        }

        String version = headers.getFirst(Headers.SEC_WEB_SOCKET_VERSION);
        if (!"13".equals(version)) {
            throw new BadRequestException("only support web socket version 13, version=" + version, "INVALID_HTTP_REQUEST");
        }  // only support latest ws version
    }

    public void shutdown() {
        logger.info("close websocket channels");
        for (WebSocketChannel channel : channels) {
            WebSockets.sendClose(WebSocketCloseCodes.SERVICE_RESTART, "server is shutting down", channel, ChannelCallback.INSTANCE);
        }
    }

    public void add(String path, ChannelHandler<?, ?> handler) {
        ChannelHandler<?, ?> previous = handlers.putIfAbsent(path, handler);
        if (previous != null) throw new Error(format("found duplicate channel listener, path={}, previousListener={}", path, previous.listener.getClass().getCanonicalName()));
    }
}
