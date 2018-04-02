package core.framework.impl.web;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.request.RequestParser;
import core.framework.impl.web.response.ResponseHandler;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.impl.web.route.Route;
import core.framework.impl.web.session.SessionManager;
import core.framework.impl.web.site.SiteManager;
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
    public final RequestBeanMapper requestBeanMapper = new RequestBeanMapper();
    public final ResponseBeanTypeValidator responseBeanTypeValidator = new ResponseBeanTypeValidator();
    public final Route route = new Route();
    public final Interceptors interceptors = new Interceptors();
    public final WebContextImpl webContext = new WebContextImpl();
    public final HTTPServerErrorHandler errorHandler;

    private final Logger logger = LoggerFactory.getLogger(HTTPServerHandler.class);
    private final LogManager logManager;
    private final SessionManager sessionManager;
    private final ResponseHandler responseHandler;
    private final HTTPServerHealthCheckHandler healthCheckHandler = new HTTPServerHealthCheckHandler();

    HTTPServerHandler(LogManager logManager, SiteManager siteManager) {
        this.logManager = logManager;
        sessionManager = siteManager.sessionManager;
        responseHandler = new ResponseHandler(responseBeanTypeValidator, siteManager.templateManager, sessionManager);
        errorHandler = new HTTPServerErrorHandler(responseHandler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String path = exchange.getRequestPath();
        if (HTTPServerHealthCheckHandler.PATH.equals(path)) {      // not treat health-check as action
            healthCheckHandler.handle(exchange.getResponseSender());
            return;
        }

        handle(path, exchange);
    }

    private void handle(String path, HttpServerExchange exchange) {
        logManager.begin("=== http transaction begin ===");
        RequestImpl request = new RequestImpl(exchange, requestBeanMapper);
        try {
            webContext.initialize(request);     // initialize webContext at beginning, the customerErrorHandler in errorHandler may use it if any exception

            ActionLog actionLog = logManager.currentActionLog();
            requestParser.parse(request, exchange, actionLog);
            request.session = sessionManager.load(request);

            HeaderMap headers = exchange.getRequestHeaders();

            String client = headers.getFirst(HTTPServerHandler.HEADER_CLIENT);
            if (client != null) actionLog.context("client", client);

            actionLog.refId(headers.getFirst(HTTPServerHandler.HEADER_REF_ID));

            ControllerHolder controller = route.get(path, request.method(), request.pathParams, actionLog);
            actionLog.action(controller.action);
            actionLog.context("controller", controller.controllerInfo);
            logger.debug("controllerClass={}", controller.controller.getClass().getCanonicalName());

            // trigger trace after action is determined due to trace log use action as part of path, is there better way?
            if ("true".equals(headers.getFirst(HEADER_TRACE))) {
                actionLog.trace = true;
            }

            Response response = new InvocationImpl(controller, interceptors, request, webContext).proceed();
            sessionManager.save(request, response);
            responseHandler.render((ResponseImpl) response, exchange);
        } catch (Throwable e) {
            logManager.logError(e);
            errorHandler.handleError(e, exchange, request);
        } finally {
            webContext.cleanup();
            logManager.end("=== http transaction end ===");
        }
    }
}
