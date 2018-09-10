package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.HTTPIOHandler;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.web.Controller;
import core.framework.web.ErrorHandler;
import core.framework.web.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class HTTPConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(HTTPConfig.class);
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    public void route(HTTPMethod method, String path, Controller controller) {
        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");
        context.route(method, path, controller, false);
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

    /**
     * Set cidr blocks to filter ingress ip, e.g. 192.168.0.1/24 or 192.168.1.1/32 for single ip
     *
     * @param cidrs cidr blocks
     */
    public void allowCIDR(String... cidrs) {
        if (context.httpServer.handler.accessControl != null) {
            throw new Error(format("allow cidr is already configured, cidrs={}, previous={}", Arrays.toString(cidrs), context.httpServer.handler.accessControl.cidrs));
        }

        logger.info("limit remote access, cidrs={}", Arrays.toString(cidrs));
        context.httpServer.handler.accessControl = new IPAccessControl(cidrs);
    }

    public void gzip() {
        context.httpServer.gzip = true;
    }
}
