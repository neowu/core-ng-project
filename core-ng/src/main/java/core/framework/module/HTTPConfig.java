package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.inject.InjectValidator;
import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPHost;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.bean.RequestBeanReader;
import core.framework.internal.web.bean.ResponseBeanWriter;
import core.framework.web.Controller;
import core.framework.web.ErrorHandler;
import core.framework.web.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class HTTPConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(HTTPConfig.class);
    ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    public void route(HTTPMethod method, String path, Controller controller) {
        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");
        context.route(method, path, controller, false);
    }

    public void route(HTTPMethod method, String path, LambdaController controller) {
        route(method, path, (Controller) controller);
    }

    public void bean(Class<?> beanClass) {
        logger.info("register bean, class={}", beanClass.getCanonicalName());
        if (beanClass.isEnum()) {   // enum is usually declared to expose constants via /_sys/api, e.g. errorCodes, or pathParams used by controller directly
            context.beanClassValidator.beanClassNameValidator.validate(beanClass);
            JSONClassValidator.validateEnum(beanClass);
        } else {
            registerBean(beanClass);
        }
        boolean added = context.apiController.beanClasses.add(beanClass);
        if (!added) throw new Error("bean class is already registered, class=" + beanClass.getCanonicalName());
    }

    // register http body bean and query param bean
    private void registerBean(Class<?> beanClass) {
        RequestBeanReader reader = context.httpServer.handler.requestBeanReader;
        if (RequestBeanReader.isQueryParamBean(beanClass)) {
            if (reader.containsQueryParam(beanClass)) {
                throw new Error("query param bean class is already registered or referred by service interface, class=" + beanClass.getCanonicalName());
            }
            reader.registerQueryParam(beanClass, context.beanClassValidator.beanClassNameValidator);
        } else {
            ResponseBeanWriter writer = context.httpServer.handler.responseBeanWriter;
            if (reader.containsBean(beanClass) || writer.contains(beanClass)) {
                throw new Error("bean class is already registered or referred by service interface, class=" + beanClass.getCanonicalName());
            }
            reader.registerBean(beanClass, context.beanClassValidator);
            writer.register(beanClass, context.beanClassValidator);
        }
    }

    public void intercept(Interceptor interceptor) {
        if (interceptor.getClass().isSynthetic())
            throw new Error("interceptor class must not be anonymous class or lambda, please use static class, interceptorClass=" + interceptor.getClass().getCanonicalName());
        new InjectValidator(interceptor).validate();
        context.httpServerConfig.interceptors.add(interceptor);
    }

    public void errorHandler(ErrorHandler handler) {
        context.httpServer.handler.errorHandler.customErrorHandler = handler;
    }

    // host is in "host:port" or "port" format, e.g. 8080 or 127.0.0.1:8080
    public void listenHTTP(String host) {
        context.httpServerConfig.httpHost = HTTPHost.parse(host);
    }

    public void listenHTTPS(String host) {
        context.httpServerConfig.httpsHost = HTTPHost.parse(host);
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
        if (maxIPs < 1) throw new Error("maxIPs must be greater than 1, maxIPs=" + maxIPs);
        context.httpServer.handler.requestParser.clientIPParser.maxForwardedIPs = maxIPs;
    }

    public AccessConfig access() {
        return new AccessConfig(context);
    }

    public void gzip() {
        context.httpServerConfig.gzip = true;
    }

    // use backend timeout of cloud lb
    public void maxProcessTime(Duration maxProcessTime) {
        context.httpServer.handler.maxProcessTimeInNano = maxProcessTime.toNanos();
    }

    // to configure max body size for both regular post and multipart upload
    public void maxEntitySize(long maxEntitySize) {
        context.httpServerConfig.maxEntitySize = maxEntitySize;
    }
}
