package core.framework.internal.web.sse;

import core.framework.http.HTTPMethod;
import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.HTTPHandlerContext;
import core.framework.internal.web.http.RateControl;
import core.framework.internal.web.request.RequestImpl;
import core.framework.internal.web.service.ErrorResponse;
import core.framework.internal.web.session.ReadOnlySession;
import core.framework.internal.web.session.SessionManager;
import core.framework.util.Strings;
import core.framework.web.sse.ChannelListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListeners;
import org.xnio.IoUtils;
import org.xnio.channels.StreamSinkChannel;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerSentEventHandler implements HttpHandler {
    // persistent connection, use longer max process time
    // though LB backend timeout is to 600s, for long time sse, it should be processed via message queue
    static final long MAX_PROCESS_TIME_IN_NANO = Duration.ofSeconds(300).toNanos();
    static final HttpString HEADER_TRACE_ID = new HttpString("x-trace-id");

    private static final HttpString LAST_EVENT_ID = new HttpString("Last-Event-ID");
    private final Logger logger = LoggerFactory.getLogger(ServerSentEventHandler.class);
    private final LogManager logManager;
    private final SessionManager sessionManager;
    private final HTTPHandlerContext handlerContext;
    private final Map<String, ChannelSupport<?>> supports = new HashMap<>();

    public ServerSentEventHandler(LogManager logManager, SessionManager sessionManager, HTTPHandlerContext handlerContext) {
        this.logManager = logManager;
        this.sessionManager = sessionManager;
        this.handlerContext = handlerContext;
    }

    public boolean check(HttpString method, String path, HeaderMap headers) {
        return "text/event-stream".equals(headers.getFirst(Headers.ACCEPT))
               && supports.containsKey(key(method.toString(), path));
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/event-stream");
        exchange.setPersistent(false);
        StreamSinkChannel sink = exchange.getResponseChannel();
        try {
            if (sink.flush()) {
                exchange.dispatch(() -> handle(exchange, sink));
            } else {
                var listener = ChannelListeners.flushingChannelListener(_ -> exchange.dispatch(() -> handle(exchange, sink)),
                    (_, e) -> {
                        logger.warn("failed to establish sse connection, error={}", e.getMessage(), e);
                        IoUtils.safeClose(exchange.getConnection());
                    });
                sink.getWriteSetter().set(listener);
                sink.resumeWrites();
            }
        } catch (IOException e) {
            logger.warn("failed to establish sse connection, error={}", e.getMessage(), e);
            IoUtils.safeClose(exchange.getConnection());
        }
    }

    void handle(HttpServerExchange exchange, StreamSinkChannel sink) {
        VirtualThread.COUNT.increase();
        long httpDelay = System.nanoTime() - exchange.getRequestStartTime();
        ActionLog actionLog = logManager.begin("=== sse connect begin ===", null);
        var request = new RequestImpl(exchange, handlerContext.requestBeanReader);
        ChannelImpl<Object> channel = null;
        try {
            logger.debug("httpDelay={}", httpDelay);
            actionLog.stats.put("http_delay", (double) httpDelay);

            handlerContext.requestParser.parse(request, exchange, actionLog);
            if (handlerContext.accessControl != null) handlerContext.accessControl.validate(request.clientIP());  // check ip before checking routing, return 403 asap

            actionLog.warningContext.maxProcessTimeInNano(MAX_PROCESS_TIME_IN_NANO);
            String path = request.path();
            @SuppressWarnings("unchecked")
            ChannelSupport<Object> support = (ChannelSupport<Object>) supports.get(key(request.method().name(), path));   // ServerSentEventHandler.check() ensures path exists
            actionLog.action("sse:" + path + ":connect");

            if (handlerContext.rateControl != null) {
                limitRate(handlerContext.rateControl, support, request.clientIP());
            }

            channel = new ChannelImpl<>(exchange, sink, support.context, support.builder, actionLog.id);
            actionLog.context("channel", channel.id);

            channel.clientIP = request.clientIP();
            String traceId = exchange.getRequestHeaders().getFirst(HEADER_TRACE_ID);    // used by frontend to trace request
            if (traceId != null) {
                actionLog.context.put("trace_id", List.of(traceId));
                channel.traceId = traceId;
            }

            sink.getWriteSetter().set(channel.writeListener);
            support.context.add(channel);
            exchange.addExchangeCompleteListener(new ServerSentEventCloseHandler<>(logManager, channel, support));

            channel.sendBytes(Strings.bytes("retry: 5000\n\n"));    // set browser retry to 5s

            request.session = ReadOnlySession.of(sessionManager.load(request, actionLog));
            String lastEventId = exchange.getRequestHeaders().getLast(LAST_EVENT_ID);
            if (lastEventId != null) actionLog.context("last_event_id", lastEventId);

            actionLog.context("listener", support.listener.getClass().getCanonicalName());
            support.listener.onConnect(request, channel, lastEventId);

            if (!channel.groups.isEmpty()) actionLog.context("group", channel.groups.toArray()); // may join group onConnect
        } catch (Throwable e) {
            logManager.logError(e);

            if (channel != null) {
                String message = errorMessage(handlerContext.responseBeanWriter.toJSON(ErrorResponse.errorResponse(e, actionLog.id)));
                channel.sendBytes(Strings.bytes(message));
                channel.close();    // gracefully shutdown connection to make sure retry/error can be sent
            }
        } finally {
            logManager.end("=== sse connect end ===");
            VirtualThread.COUNT.decrease();
        }
    }

    void limitRate(RateControl rateControl, ChannelSupport<Object> support, String clientIP) {
        if (support.limitRate != null) {
            String group = support.limitRate.value();
            rateControl.validateRate(group, clientIP);
        }
    }

    String errorMessage(String errorResponse) {
        return "retry: 86400000\n\n"
               + "event: error\n"
               + "data: " + errorResponse + "\n\n";
    }

    public <T> void add(HTTPMethod method, String path, Class<T> eventClass, ChannelListener<T> listener, ServerSentEventContextImpl<T> context) {
        var previous = supports.put(key(method.name(), path), new ChannelSupport<>(listener, eventClass, context));
        if (previous != null) throw new Error(Strings.format("found duplicate sse listener, method={}, path={}", method, path));
    }

    public void shutdown() {
        logger.info("close sse connections");
        for (ChannelSupport<?> support : supports.values()) {
            for (var channel : support.context.all()) {
                ((ChannelImpl<?>) channel).shutdown();
            }
        }
    }

    private String key(String method, String path) {
        return method + ":" + path;
    }
}
