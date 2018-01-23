package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.http.AllowSourceIPInterceptor;
import core.framework.impl.web.http.LimitRateInterceptor;
import core.framework.util.Exceptions;
import core.framework.web.ErrorHandler;
import core.framework.web.Interceptor;

import java.util.Set;

/**
 * @author neo
 */
public final class HTTPConfig {
    private final ModuleContext context;
    private final State state;

    HTTPConfig(ModuleContext context) {
        this.context = context;
        state = context.config.http();
    }

    public void httpPort(int port) {
        context.httpServer.httpPort = port;
    }

    public void httpsPort(int port) {
        context.httpServer.httpsPort = port;
    }

    public void intercept(Interceptor interceptor) {
        context.httpServer.handler.interceptors.add(interceptor);
    }

    public void errorHandler(ErrorHandler handler) {
        context.httpServer.handler.errorHandler.customErrorHandler = handler;
    }

    public LimitRateConfig limitRate() {
        if (state.limitRateInterceptor == null) {
            state.limitRateInterceptor = new LimitRateInterceptor();
            intercept(state.limitRateInterceptor);
        }
        return new LimitRateConfig(state);
    }

    /**
     * Set max x-forwarded-for ips to prevent client ip spoofing, e.g. script clients send custom x-forwarded-for header to bypass rate limiting by ip.
     * Default is 2 to fit common scenarios, e.g. Google LB(append 2 ips)->kube service, AWS->nginx->webapp
     *
     * @param maxIPs the max number for forwarded ips
     */
    public void maxForwardedIPs(int maxIPs) {
        if (maxIPs < 1) throw Exceptions.error("maxIPs must be greater than 1, maxIPs={}", maxIPs);
        context.httpServer.handler.requestParser.clientIPParser.maxForwardedIPs = maxIPs;
    }

    public void allowSourceIPs(Set<String> sourceIPs) {
        context.httpServer.handler.interceptors.add(new AllowSourceIPInterceptor(sourceIPs));
    }

    public void enableGZip() {
        context.httpServer.gzip = true;
    }

    public static class State {
        LimitRateInterceptor limitRateInterceptor;
        boolean limitRateGroupAdded;

        public void validate() {
            if (limitRateInterceptor != null && !limitRateGroupAdded) {
                throw new Error("limitRate() is configured but no group added, please remove unnecessary config");
            }
        }
    }
}
