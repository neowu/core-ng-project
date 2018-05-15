package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.http.ClientIPInterceptor;
import core.framework.impl.web.http.LimitRateInterceptor;
import core.framework.util.Exceptions;
import core.framework.web.ErrorHandler;
import core.framework.web.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author neo
 */
public final class HTTPConfig implements Config {
    private final Logger logger = LoggerFactory.getLogger(HTTPConfig.class);
    private final ModuleContext context;

    LimitRateInterceptor limitRateInterceptor;
    boolean limitRateGroupAdded;

    HTTPConfig(ModuleContext context) {
        this.context = context;
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
        if (limitRateInterceptor == null) {
            limitRateInterceptor = new LimitRateInterceptor();
            intercept(limitRateInterceptor);
        }
        return new LimitRateConfig(this);
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
     * @param cidrs cidr blocks
     */
    public void allowCIDR(String... cidrs) {
        logger.info("limit remote access, cidrs={}", Arrays.toString(cidrs));
        context.httpServer.handler.interceptors.add(new ClientIPInterceptor(cidrs));
    }

    public void enableGZip() {
        context.httpServer.gzip = true;
    }

    @Override
    public void validate() {
        if (limitRateInterceptor != null && !limitRateGroupAdded) {
            throw new Error("limitRate() is configured but no group added, please remove unnecessary config");
        }
    }
}
