package core.framework.impl.web;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.bean.BeanClassNameValidator;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.controller.ControllerHolder;
import core.framework.impl.web.controller.Interceptors;
import core.framework.impl.web.controller.InvocationImpl;
import core.framework.impl.web.controller.WebContextImpl;
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

/**
 * @author neo
 */
public class HTTPServerHandler implements HttpHandler {
    public static final HttpString HEADER_REF_ID = new HttpString("ref-id");
    public static final HttpString HEADER_TRACE = new HttpString("trace");
    public static final HttpString HEADER_CLIENT = new HttpString("client");

    public final RequestParser requestParser = new RequestParser();
    public final Route route = new Route();
    public final Interceptors interceptors = new Interceptors();
    public final WebContextImpl webContext = new WebContextImpl();
    public final HTTPServerErrorHandler errorHandler;
    public final WebSocketHandler webSocketHandler;
    private final BeanClassNameValidator beanClassNameValidator = new BeanClassNameValidator();
    public final RequestBeanMapper requestBeanMapper = new RequestBeanMapper(beanClassNameValidator);
    public final ResponseBeanMapper responseBeanMapper = new ResponseBeanMapper(beanClassNameValidator);

    private final Logger logger = LoggerFactory.getLogger(HTTPServerHandler.class);
    private final LogManager logManager;
    private final SessionManager sessionManager;
    private final ResponseHandler responseHandler;

    HTTPServerHandler(LogManager logManager, SessionManager sessionManager, TemplateManager templateManager) {
        this.logManager = logManager;
        this.sessionManager = sessionManager;
        responseHandler = new ResponseHandler(responseBeanMapper, templateManager);
        errorHandler = new HTTPServerErrorHandler(responseHandler);
        webSocketHandler = new WebSocketHandler(logManager);
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
            HeaderMap headers = exchange.getRequestHeaders();
            linkContext(actionLog, headers);
            request.session = sessionManager.load(request);

            if (webSocketHandler.isWebSocket(request.method(), headers)) {
                webSocketHandler.handle(exchange, request, actionLog);
                return; // with websocket, it doesn't save session
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
            webContext.cleanup();
            logManager.end("=== http transaction end ===");
        }
    }

    void linkContext(ActionLog actionLog, HeaderMap headers) {
        String client = headers.getFirst(HTTPServerHandler.HEADER_CLIENT);
        if (client != null) actionLog.context("client", client);
        actionLog.refId(headers.getFirst(HTTPServerHandler.HEADER_REF_ID));
        if ("true".equals(headers.getFirst(HEADER_TRACE))) {
            actionLog.trace = true;
        }
    }
}
