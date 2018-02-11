package core.framework.impl.web.http;

import core.framework.util.Network;
import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Response;
import core.framework.web.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class ClientIPInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(ClientIPInterceptor.class);
    private final List<CIDR> cidrs;

    public ClientIPInterceptor(Set<String> cidrs) {
        this.cidrs = new ArrayList<>(cidrs.size());
        for (String cidr : cidrs) {
            this.cidrs.add(new CIDR(cidr));
        }
    }

    @Override
    public Response intercept(Invocation invocation) throws Exception {
        String clientIP = invocation.context().request().clientIP();
        validateClientIP(clientIP);
        return invocation.proceed();
    }

    void validateClientIP(String clientIP) {
        boolean allow = allow(clientIP);
        if (!allow) {
            logger.debug("ip does not match any cidr, cidrs={}", cidrs);
            throw new ForbiddenException("access denied");
        }
    }

    private boolean allow(String clientIP) {
        if (Network.isLocalAddress(clientIP)) return true;
        byte[] address = CIDR.address(clientIP);
        for (CIDR cidr : cidrs) {
            if (cidr.matches(address)) return true;
        }
        return false;
    }
}
