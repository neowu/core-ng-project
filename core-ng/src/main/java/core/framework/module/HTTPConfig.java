package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.site.AJAXErrorResponse;
import core.framework.web.Controller;
import core.framework.web.ErrorHandler;
import core.framework.web.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class HTTPConfig extends Config {
    final Set<Class<?>> beanClasses = new HashSet<>();  // custom bean classes to publish via /_sys/api
    private final Logger logger = LoggerFactory.getLogger(HTTPConfig.class);
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        beanClasses.add(AJAXErrorResponse.class);   // publish default ajax error response
    }

    public void route(HTTPMethod method, String path, Controller controller) {
        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");
        context.route(method, path, controller, false);
    }

    public void route(HTTPMethod method, String path, LambdaController controller) {
        route(method, path, (Controller) controller);
    }

    public void bean(Class<?> beanClass) {
        logger.info("register bean, class=" + beanClass.getCanonicalName());
        if (beanClass.isEnum()) {   // enum is usually declared to expose constants via /_sys/api, e.g. errorCodes, or pathParams used by controller directly
            context.beanClassNameValidator.validate(beanClass);
            JSONClassValidator.validateEnum(beanClass);
        } else {
            context.bean(beanClass);
        }
        boolean added = this.beanClasses.add(beanClass);
        if (!added) throw new Error("bean class is already registered, class=" + beanClass.getCanonicalName());
    }

    public void intercept(Interceptor interceptor) {
        context.httpServer.handler.interceptors.add(interceptor);
    }

    public void errorHandler(ErrorHandler handler) {
        context.httpServer.handler.errorHandler.customErrorHandler = handler;
    }

    public void httpPort(int port) {
        context.httpServer.httpPort = port;
    }

    public void httpsPort(int port) {
        context.httpServer.httpsPort = port;
    }

    public LimitRateConfig limitRate() {
        return context.config(LimitRateConfig.class, null);
    }

    /**
     * Set max x-forwarded-for ips to prevent client ip spoofing, e.g. script clients send custom x-forwarded-for header to bypass rate limiting by ip.
     * Default is 2 to fit common scenarios, e.g. Google LB(append 2 ips)->kube service, AWS->nginx->webapp
     *
     * @param maxIPs the max number for forwarded ips
     */
    public void maxForwardedIPs(int maxIPs) {
        if (maxIPs < 1) throw new Error(format("maxIPs must be greater than 1, maxIPs={}", maxIPs));
        context.httpServer.handler.requestParser.clientIPParser.maxForwardedIPs = maxIPs;
    }

    public AccessConfig access() {
        return new AccessConfig(context);
    }

    public void gzip() {
        context.httpServer.gzip = true;
    }
}
