package core.framework.impl.web;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.bean.BeanMapperRegistry;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.controller.ControllerHolder;
import core.framework.impl.web.controller.Interceptors;
import core.framework.impl.web.controller.InvocationImpl;
import core.framework.impl.web.controller.WebContextImpl;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.request.RequestParser;
import core.framework.impl.web.response.ResponseHandler;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.impl.web.route.Route;
import core.framework.impl.web.session.SessionManager;
import core.framework.impl.web.site.TemplateManager;
import core.framework.impl.web.websocket.WebSocketHandler;
import core.framework.web.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public class HTTPHandler implements HttpHandler {
    public static final HttpString HEADER_CORRELATION_ID = new HttpString("correlation-id");
    public static final HttpString HEADER_CLIENT = new HttpString("client");
    public static final HttpString HEADER_REF_ID = new HttpString("ref-id");
    public static final HttpString HEADER_TRACE = new HttpString("trace");

    public final RequestParser requestParser = new RequestParser();
    public final Route route = new Route();
    public final Interceptors interceptors = new Interceptors();
    public final WebContextImpl webContext = new WebContextImpl();
    public final HTTPErrorHandler errorHandler;

    public final BeanMapperRegistry beanMapperRegistry = new BeanMapperRegistry();
    public final RequestBeanMapper requestBeanMapper = new RequestBeanMapper(beanMapperRegistry);
    public final ResponseBeanMapper responseBeanMapper = new ResponseBeanMapper(beanMapperRegistry);

    private final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);
    private final LogManager logManager;
    private final SessionManager sessionManager;
    private final ResponseHandler responseHandler;

    public WebSocketHandler webSocketHandler;
    public IPAccessControl accessControl;

    HTTPHandler(LogManager logManager, SessionManager sessionManager, TemplateManager templateManager, ShutdownHandler shutdownHandler) {
        this.logManager = logManager;
        this.sessionManager = sessionManager;
        responseHandler = new ResponseHandler(responseBeanMapper, templateManager, shutdownHandler);
        errorHandler = new HTTPErrorHandler(responseHandler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);  // in io handler form parser will dispatch to current io thread
            return;
        }

        handle(exchange);
    }

    private void handle(HttpServerExchange exchange) {
        ActionLog actionLog = logManager.begin("=== http transaction begin ===");
        var request = new RequestImpl(exchange, requestBeanMapper);
        try {
            webContext.initialize(request);
            requestParser.parse(request, exchange, actionLog);

            if (accessControl != null) accessControl.validate(request.clientIP());  // check ip before checking routing/web socket, return 403 asap

            HeaderMap headers = exchange.getRequestHeaders();
            linkContext(actionLog, headers);
            request.session = sessionManager.load(request);

            if (webSocketHandler != null && webSocketHandler.checkWebSocket(request.method(), headers)) {
                webSocketHandler.handle(exchange, request, actionLog);
                return; // with WebSocket, not save session
            }

            ControllerHolder controller = route.get(request.path(), request.method(), request.pathParams, actionLog);
            actionLog.action(controller.action);
            actionLog.context("controller", controller.controllerInfo);
            logger.debug("controllerClass={}", controller.controller.getClass().getCanonicalName());

            Response response = new InvocationImpl(controller, interceptors, request, webContext).proceed();
            sessionManager.save(request, response);
            responseHandler.render((ResponseImpl) response, exchange, actionLog);
        } catch (Throwable e) {
            logManager.logError(e);
            errorHandler.handleError(e, exchange, request, actionLog);
        } finally {
            // refer to io.undertow.io.AsyncSenderImpl.send(java.nio.ByteBuffer, io.undertow.io.IoCallback),
            // sender.send() will write response until can't write more, then call channel.resumeWrites(), which will resume after this finally block finished, so this can be small delay
            webContext.cleanup();
            logManager.end("=== http transaction end ===");
        }
    }

    void linkContext(ActionLog actionLog, HeaderMap headers) {
        String client = headers.getFirst(HTTPHandler.HEADER_CLIENT);
        if (client != null) actionLog.clients = List.of(client);

        String refId = headers.getFirst(HTTPHandler.HEADER_REF_ID);
        if (refId != null) actionLog.refIds = List.of(refId);

        String correlationId = headers.getFirst(HTTPHandler.HEADER_CORRELATION_ID);
        if (correlationId != null) actionLog.correlationIds = List.of(correlationId);

        if ("true".equals(headers.getFirst(HEADER_TRACE)))
            actionLog.trace = true;
    }
}
