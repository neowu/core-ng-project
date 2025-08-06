package core.framework.internal.web.http;

import core.framework.web.exception.ForbiddenException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author neo
 */
public class IPAccessControl {
    private final Logger logger = LoggerFactory.getLogger(IPAccessControl.class);
    @Nullable
    public IPv4Ranges allow;
    @Nullable
    public IPv4Ranges deny;
    @Nullable
    public IPv6Ranges allowIPv6;
    @Nullable
    public IPv6Ranges denyIPv6;

    public void validate(String clientIP) {
        InetAddress address;
        try {
            address = InetAddress.getByName(clientIP);
        } catch (UnknownHostException e) {
            throw new Error(e);  // client ip format is already validated in ClientIPParser, so here it won't resolve DNS
        }

        if (isLocal(address)) {
            logger.debug("allow site local client address");
            return;
        }

        byte[] byteAddress = address.getAddress();
        IPRanges allow;
        IPRanges deny;
        if (byteAddress.length == 4) {
            allow = this.allow;
            deny = this.deny;
        } else if (byteAddress.length == 16) {
            allow = allowIPv6;
            deny = denyIPv6;
        } else {
            throw new Error("unexpected address, address=" + address);
        }
        if (!allow(byteAddress, allow, deny)) {
            throw new ForbiddenException("access denied", "IP_ACCESS_DENIED");
        }
    }

    boolean isLocal(InetAddress address) {
        return address.isLoopbackAddress() || address.isSiteLocalAddress();
    }

    boolean allow(byte[] address, @Nullable IPRanges allow, @Nullable IPRanges deny) {
        if (allow != null && allow.matches(address)) {
            logger.debug("allow client ip within allowed ranges");
            return true;
        }

        if (deny == null || deny.matches(address)) {    // if deny == null, it blocks all
            logger.debug("deny client ip within denied ranges");
            return false;
        }

        logger.debug("allow client ip not within denied ranges");
        return true;
    }
}
