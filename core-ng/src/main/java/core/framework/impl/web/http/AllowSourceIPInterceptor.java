package core.framework.impl.web.http;

import core.framework.util.Network;
import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Response;
import core.framework.web.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String clientIP = invocation.context().request().clientIP();
        validateSourceIP(clientIP);
        return invocation.proceed();
    }

    void validateSourceIP(String clientIP) {
        logger.debug("validate clientIP, clientIP={}, allowedSourceIPs={}", clientIP, allowedSourceIPs);
        boolean allow = allowedSourceIPs.contains(clientIP) || Network.isLocalAddress(clientIP);
        if (!allow) {
            throw new ForbiddenException("access denied");
        }
    }
}
