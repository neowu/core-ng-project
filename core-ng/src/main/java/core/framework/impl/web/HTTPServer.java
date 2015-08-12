package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.module.HTTPConfig;
import core.framework.api.module.RouteConfig;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import core.framework.api.web.Controller;
import core.framework.api.web.ErrorHandler;
import core.framework.api.web.Interceptor;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.response.ResponseHandler;
import core.framework.impl.web.route.Route;
import core.framework.impl.web.site.SiteManager;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public class HTTPServer implements RouteConfig, HTTPConfig {
    static {
        // make undertow to use slf4j
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    public final SiteManager siteManager = new SiteManager();
    public final BeanValidator validator = new BeanValidator();
    public final WebContextImpl webContext = new WebContextImpl();
    public final Route route = new Route();
    private final List<Interceptor> interceptors = Lists.newArrayList();
    private final HTTPServerHandler httpServerHandler;
    private final HTTPServerErrorHandler errorHandler;
    private int port = 8080;

    public HTTPServer(LogManager logManager) {
        ResponseHandler responseHandler = new ResponseHandler(validator, siteManager.templateManager);
        errorHandler = new HTTPServerErrorHandler(responseHandler, logManager);

        httpServerHandler = new HTTPServerHandler();
        httpServerHandler.logManager = logManager;
        httpServerHandler.route = route;
        httpServerHandler.interceptors = interceptors;
        httpServerHandler.sessionManager = siteManager.sessionManager;
        httpServerHandler.webContext = webContext;
        httpServerHandler.validator = validator;
        httpServerHandler.responseHandler = responseHandler;
        httpServerHandler.errorHandler = errorHandler;
    }

    public void start() {
        StopWatch watch = new StopWatch();
        try {
            Undertow server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(httpServerHandler)
                .build();
            server.start();
        } finally {
            logger.info("http server started, elapsedTime={}", watch.elapsedTime());
        }
    }

    @Override
    public void intercept(Interceptor interceptor) {
        if (interceptor.getClass().isSynthetic())
            throw Exceptions.error("interceptor class must not be anonymous class or lambda, please create static class, interceptorClass={}", interceptor.getClass().getCanonicalName());

        interceptors.add(interceptor);
    }

    @Override
    public void errorHandler(ErrorHandler handler) {
        errorHandler.customErrorHandler = handler;
    }

    @Override
    public void add(HTTPMethod method, String path, Controller controller) {
        route.add(method, path, new ControllerHolder(controller, null));
    }

    @Override
    public void port(int port) {
        this.port = port;
    }
}
