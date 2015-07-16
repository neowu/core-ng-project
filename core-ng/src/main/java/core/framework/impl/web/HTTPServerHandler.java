package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Charsets;
import core.framework.api.util.InputStreams;
import core.framework.api.util.Maps;
import core.framework.api.web.Interceptor;
import core.framework.api.web.Response;
import core.framework.api.web.ResponseImpl;
import core.framework.impl.log.ActionLogger;
import core.framework.impl.web.response.ResponseHandler;
import core.framework.impl.web.route.Route;
import core.framework.impl.web.session.SessionManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.HeaderValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author neo
 */
public class HTTPServerHandler implements HttpHandler {
    public static final String HEADER_REQUEST_ID = "request-id";
    public static final String HEADER_TRACE = "trace";

    private final Logger logger = LoggerFactory.getLogger(HTTPServerHandler.class);

    private final FormParserFactory formParserFactory = FormParserFactory.builder().build();
    private final ActionLogger actionLogger;
    private final Route route;
    private final List<Interceptor> interceptors;
    private final SessionManager sessionManager;
    private final WebContextImpl webContext;
    private final BeanValidator validator;
    private final ResponseHandler responseHandler;
    final HTTPServerErrorHandler errorHandler;

    public HTTPServerHandler(ActionLogger actionLogger, Route route, List<Interceptor> interceptors, SessionManager sessionManager, WebContextImpl webContext, BeanValidator validator) {
        this.actionLogger = actionLogger;
        this.route = route;
        this.interceptors = interceptors;
        this.sessionManager = sessionManager;
        this.webContext = webContext;
        this.validator = validator;
        responseHandler = new ResponseHandler(validator);
        errorHandler = new HTTPServerErrorHandler(responseHandler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        RequestImpl request = new RequestImpl(exchange, validator);

        try {
            actionLogger.start();
            logger.debug("=== http transaction begin ===");

            parseRequest(request, exchange);

            String requestId = request.header(HEADER_REQUEST_ID).orElseGet(() -> UUID.randomUUID().toString());
            ActionLogContext.put(ActionLogContext.REQUEST_ID, requestId);

            ControllerProxy controller = route.get(request.path(), request.method, request.pathParams);
            ActionLogContext.put(ActionLogContext.ACTION, controller.action);
            ActionLogContext.put("controller", controller.targetClassName + "." + controller.targetMethodName);
            logger.debug("controllerClass={}", controller.controller.getClass().getCanonicalName());

            // trigger trace after action/requestId are determined due to trace log use action as part of path, is there better way?
            if ("true".equals(exchange.getRequestHeaders().getFirst(HEADER_TRACE))) {
                logger.warn("trace log is triggered for current request, requestId={}, clientIP={}", requestId, request.clientIP());
                ActionLogContext.put(ActionLogContext.TRACE, Boolean.TRUE);
            }

            webContext.context.set(Maps.newHashMap());
            Response response = new InvocationImpl(controller, interceptors, request, webContext).proceed();
            sessionManager.save(request, exchange);
            responseHandler.handle((ResponseImpl) response, exchange);
        } catch (Throwable e) {
            errorHandler.handleError(e, exchange, request);
        } finally {
            webContext.context.remove();
            logger.debug("=== http transaction end ===");
            actionLogger.end();
        }
    }

    private void parseRequest(RequestImpl request, HttpServerExchange exchange) throws IOException {
        logger.debug("requestURL={}", request.requestURL());
        logger.debug("queryString={}", exchange.getQueryString());
        for (HeaderValues header : exchange.getRequestHeaders()) {
            logger.debug("[header] {}={}", header.getHeaderName(), header.toArray());
        }
        String path = exchange.getRequestPath();
        ActionLogContext.put("path", path);
        ActionLogContext.put("method", String.valueOf(request.method));
        logger.debug("clientIP={}", request.remoteAddress);

        if (request.contentType != null) {
            parseRequestBody(exchange, request);
        }

        request.session = sessionManager.load(request);
    }

    private void parseRequestBody(HttpServerExchange exchange, RequestImpl request) throws IOException {
        if (request.contentType.startsWith("application/json")) {
            exchange.startBlocking();
            byte[] bytes = InputStreams.bytes(exchange.getInputStream());
            request.body = new String(bytes, Charsets.UTF_8);
            logger.debug("body={}", request.body);
        } else if (request.method == HTTPMethod.POST) {
            FormDataParser parser = formParserFactory.createParser(exchange);
            if (parser != null) {
                request.formData = parser.parseBlocking();
                for (String name : request.formData) {
                    logger.debug("[form] {}={}", name, request.formData.get(name));
                }
            }
        }
    }
}
