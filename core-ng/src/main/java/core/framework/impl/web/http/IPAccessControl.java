package core.framework.impl.web.http;

import core.framework.web.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author neo
 */
public class IPAccessControl {
    private final Logger logger = LoggerFactory.getLogger(IPAccessControl.class);
    private final List<CIDR> cidrs = new ArrayList<>();

    public void allowCIDR(String... cidrs) {
        Arrays.stream(cidrs).map(CIDR::new).forEach(this.cidrs::add);
    }

    public void validateClientIP(String clientIP) {
        boolean allow = allow(clientIP);
        if (!allow) {
            logger.debug("ip is not from local or match any cidr, cidrs={}", cidrs);
            throw new ForbiddenException("access denied");
        }
    }

    private boolean allow(String clientIP) {
        InetAddress address = address(clientIP);
        if (isLocal(address)) return true;
        byte[] ipAddress = address.getAddress();
        for (CIDR cidr : cidrs) {
            if (cidr.matches(ipAddress)) return true;
        }
        return false;
    }

    boolean isLocal(InetAddress address) {
        return address.isLoopbackAddress() || address.isSiteLocalAddress();
    }

    private InetAddress address(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }
}
