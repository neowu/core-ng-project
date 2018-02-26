package core.framework.module;

import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.http.ClientIPInterceptor;
import core.framework.impl.web.http.LimitRateInterceptor;
import core.framework.util.Exceptions;
import core.framework.web.ErrorHandler;
import core.framework.web.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author neo
 */
public final class HTTPConfig {
    private final Logger logger = LoggerFactory.getLogger(HTTPConfig.class);
    private final ModuleContext context;
    private final State state;

    HTTPConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("http", State::new);
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

    /**
     * Set cidr blocks to filter ingress ip, e.g. 192.168.0.1/24 or 192.168.1.1/32 for single ip
     *
     * @param cidrs cidr block
     */
    public void allowClientIP(Set<String> cidrs) {
        logger.info("only allow remote access from {}", cidrs);
        context.httpServer.handler.interceptors.add(new ClientIPInterceptor(cidrs));
    }

    public void enableGZip() {
        context.httpServer.gzip = true;
    }

    public static class State implements Config.State {
        LimitRateInterceptor limitRateInterceptor;
        boolean limitRateGroupAdded;

        @Override
        public void validate() {
            if (limitRateInterceptor != null && !limitRateGroupAdded) {
                throw new Error("limitRate() is configured but no group added, please remove unnecessary config");
            }
        }
    }
}
