package core.framework.impl.web.http;

import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * @author neo
 */
public class AllowSourceIPInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(AllowSourceIPInterceptor.class);
    private final Set<String> allowedSourceIPs;

    public AllowSourceIPInterceptor(Set<String> sourceIPs) {
        this.allowedSourceIPs = sourceIPs;
    }

    @Override
    public Response intercept(Invocation invocation) throws Exception {
        Request request = invocation.context().request();
        String clientIP = request.clientIP();
        if (!allow(clientIP)) {
            logger.debug("clientIP is not allowed, clientIP={}, allowedSourceIPs={}", clientIP, allowedSourceIPs);
            throw new ForbiddenException("access not allowed");
        }
        return invocation.proceed();
    }

    boolean allow(String clientIP) {
        if (allowedSourceIPs.contains(clientIP)) return true;
        try {
            InetAddress address = InetAddress.getByName(clientIP);
            if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
                return true;
            }
        } catch (UnknownHostException e) {
            logger.warn("unknown clientIP format, clientIP={}", clientIP, e);
        }
        return false;
    }
}
