package core.framework.impl.web.http;

import core.framework.web.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class IPAccessControl {
    public final List<CIDR> cidrs;
    private final Logger logger = LoggerFactory.getLogger(IPAccessControl.class);

    public IPAccessControl(String... cidrs) {
        this.cidrs = cidrs(cidrs);
    }

    public void validate(String clientIP) {
        boolean allow = allow(clientIP);
        if (!allow) {
            logger.debug("ip is not from local or match any cidr, cidrs={}", cidrs);
            throw new ForbiddenException("access denied");
        }
    }

    private List<CIDR> cidrs(String... cidrs) {
        if (cidrs.length == 0) return null;
        return Arrays.stream(cidrs).map(CIDR::new).collect(Collectors.toList());
    }

    private InetAddress address(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    boolean isLocal(InetAddress address) {
        return address.isLoopbackAddress() || address.isSiteLocalAddress();
    }

    private boolean allow(String clientIP) {
        InetAddress address = address(clientIP);
        if (isLocal(address)) return true;
        if (cidrs != null) {
            byte[] ipAddress = address.getAddress();
            for (CIDR cidr : cidrs) {
                if (cidr.matches(ipAddress)) return true;
            }
        }
        return false;
    }
}
