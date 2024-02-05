package core.framework.internal.web;

import core.framework.internal.async.ThreadPools;
import core.framework.internal.async.VirtualThread;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.internal.web.bean.RequestBeanReader;
import core.framework.internal.web.bean.ResponseBeanWriter;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.internal.web.controller.InvocationImpl;
import core.framework.internal.web.controller.WebContextImpl;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.internal.web.http.RateControl;
import core.framework.internal.web.request.RequestImpl;
import core.framework.internal.web.request.RequestParser;
import core.framework.internal.web.response.ResponseHandler;
import core.framework.internal.web.response.ResponseImpl;
import core.framework.internal.web.route.Route;
import core.framework.internal.web.session.SessionManager;
import core.framework.internal.web.site.TemplateManager;
import core.framework.internal.web.websocket.WebSocketHandler;
import core.framework.module.WebSocketConfig;
import core.framework.web.Interceptor;
import core.framework.web.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/**
 * @author neo
 */
public class HTTPHandler implements HttpHandler {
    public static final HttpString HEADER_CORRELATION_ID = new HttpString("correlation-id");
    public static final HttpString HEADER_CLIENT = new HttpString("client");
    public static final HttpString HEADER_REF_ID = new HttpString("ref-id");
    public static final HttpString HEADER_TRACE = new HttpString("trace");
    public static final HttpString HEADER_TIMEOUT = new HttpString("timeout");      // there is ietf draft to define Request-Timeout header, but didn't move on, so here to use shorter name

    public final RequestParser requestParser = new RequestParser();
    public final Route route = new Route();
    public final WebContextImpl webContext = new WebContextImpl();
    public final HTTPErrorHandler errorHandler;

    public final RequestBeanReader requestBeanReader = new RequestBeanReader();
    public final ResponseBeanWriter responseBeanWriter = new ResponseBeanWriter();

    public final RateControl rateControl = new RateControl();
    final ExecutorService worker = ThreadPools.virtualThreadExecutor("http-handler-");

    private final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);
    private final LogManager logManager;
    private final SessionManager sessionManager;
    private final ResponseHandler responseHandler;
    private final Semaphore semaphore = new Semaphore(Runtime.getRuntime().availableProcessors() * 32);

    public Interceptor[] interceptors;
    public WebSocketHandler webSocketHandler;
    public IPv4AccessControl accessControl;
    public long maxProcessTimeInNano = Duration.ofSeconds(30).toNanos();    // the default backend timeout of popular cloud lb (gcloud/azure) is 30s

    HTTPHandler(LogManager logManager, SessionManager sessionManager, TemplateManager templateManager) {
        this.logManager = logManager;
        this.sessionManager = sessionManager;
        responseHandler = new ResponseHandler(responseBeanWriter, templateManager, sessionManager);
        errorHandler = new HTTPErrorHandler(responseHandler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(worker, this);  // in io handler form parser will dispatch to current io thread
            return;
        }

        handle(exchange);
    }

    private void handle(HttpServerExchange exchange) {
        semaphore.acquireUninterruptibly();
        VirtualThread.COUNT.increase();
        long httpDelay = System.nanoTime() - exchange.getRequestStartTime();
        ActionLog actionLog = logManager.begin("=== http transaction begin ===", null);
        var request = new RequestImpl(exchange, requestBeanReader);
        try {
            webContext.initialize(request);

            logger.debug("httpDelay={}", httpDelay);    // http delay includes request body parsing time, it could be long if client sent post body slowly, and it is usually low, so not to use Duration format
            actionLog.stats.put("http_delay", (double) httpDelay);

            requestParser.parse(request, exchange, actionLog);

            if (accessControl != null) accessControl.validate(request.clientIP());  // check ip before checking routing/web socket, return 403 asap

            HeaderMap headers = exchange.getRequestHeaders();
            linkContext(actionLog, headers);

            if (webSocketHandler != null && webSocketHandler.checkWebSocket(request.method(), headers)) {
                rateControl.validateRate(WebSocketConfig.WS_OPEN_GROUP, request.clientIP());
                webSocketHandler.handle(exchange, request, actionLog);
                return; // with WebSocket, not save session
            }

            ControllerHolder controller = route.get(request.path(), request.method(), request.pathParams, actionLog);
            actionLog.action(controller.action);
            actionLog.context.put("controller", List.of(controller.controllerInfo));
            logger.debug("controller={}", controller.controllerInfo);
            if (controller.warnings != null) actionLog.initializeWarnings(controller.warnings);

            request.session = sessionManager.load(request, actionLog);  // load session as late as possible, so for sniffer/scan request with sessionId, it won't call redis every time even for 404/405

            Response response = new InvocationImpl(controller, interceptors, request, webContext).proceed();
            webContext.handleResponse(response);

            addKeepAliveHeader(exchange);
            responseHandler.render(request, (ResponseImpl) response, exchange, actionLog);
        } catch (Throwable e) {
            logManager.logError(e);
            errorHandler.handleError(e, exchange, request, actionLog);
        } finally {
            // refer to io.undertow.io.AsyncSenderImpl.send(java.nio.ByteBuffer, io.undertow.io.IoCallback),
            // sender.send() will write response until can't write more, then call channel.resumeWrites(), which will resume after this finally block finished, so this can be small delay
            webContext.cleanup();
            logManager.end("=== http transaction end ===");
            VirtualThread.COUNT.decrease();
            semaphore.release();
        }
    }

    void addKeepAliveHeader(HttpServerExchange exchange) {
        String keepAlive = Headers.KEEP_ALIVE.toString();
        if (keepAlive.equals(exchange.getRequestHeaders().getFirst(Headers.CONNECTION))) {
            exchange.getResponseHeaders().put(Headers.CONNECTION, keepAlive);
        }
    }

    void linkContext(ActionLog actionLog, HeaderMap headers) {
        String client = headers.getFirst(HEADER_CLIENT);
        if (client != null) actionLog.clients = List.of(client);

        String refId = headers.getFirst(HEADER_REF_ID);
        if (refId != null) actionLog.refIds = List.of(refId);

        String correlationId = headers.getFirst(HEADER_CORRELATION_ID);
        if (correlationId != null) actionLog.correlationIds = List.of(correlationId);

        String trace = headers.getFirst(HEADER_TRACE);
        if (trace != null) actionLog.trace = Trace.parse(trace);

        actionLog.warningContext.maxProcessTimeInNano(maxProcessTime(headers.getFirst(HEADER_TIMEOUT)));
    }

    long maxProcessTime(String timeout) {
        if (timeout != null) {
            try {
                return Long.parseLong(timeout);
            } catch (NumberFormatException e) {
                // ignore if got invalid timeout header from internet
                return maxProcessTimeInNano;
            }
        }
        return maxProcessTimeInNano;
    }
}
