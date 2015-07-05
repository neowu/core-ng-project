package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.module.HTTPConfig;
import core.framework.api.module.RouteConfig;
import core.framework.api.util.Exceptions;
import core.framework.api.web.Controller;
import core.framework.api.web.ErrorHandler;
import core.framework.api.web.Interceptor;
import core.framework.impl.log.ActionLogger;
import core.framework.impl.web.route.Route;
import core.framework.impl.web.session.SessionManager;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class HTTPServer implements RouteConfig, HTTPConfig {
    static {
        // make undertow to use slf4j
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    public final SessionManager sessionManager = new SessionManager();
    public final BeanValidator validator = new BeanValidator();
    public final WebContextImpl webContext = new WebContextImpl();
    private final Route route = new Route();
    private final HTTPServerHandler httpServerHandler;
    private int port = 8080;

    public HTTPServer(ActionLogger actionLogger) {
        httpServerHandler = new HTTPServerHandler(route, sessionManager, actionLogger, validator, webContext);
    }

    public void start() {
        Undertow server = Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(httpServerHandler)
//            .setWorkerThreads(200)
            .build();

        server.start();
        logger.info("http server started");
    }

    @Override
    public void intercept(Interceptor interceptor) {
        if (interceptor.getClass().isSynthetic())
            throw Exceptions.error("interceptor class must not be anonymous class or lambda, please create static class, interceptorClass={}", interceptor.getClass().getCanonicalName());

        httpServerHandler.interceptors.add(interceptor);
    }

    @Override
    public void errorHandler(ErrorHandler handler) {
        httpServerHandler.errorHandler.customErrorHandler = handler;
    }

    @Override
    public void add(HTTPMethod method, String path, Controller controller) {
        route.add(method, path, controller);
    }

    @Override
    public void port(int port) {
        this.port = port;
    }
}
