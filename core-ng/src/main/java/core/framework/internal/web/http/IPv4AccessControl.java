package core.framework.internal.web.http;

import core.framework.web.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author neo
 */
public class IPv4AccessControl {
    private final Logger logger = LoggerFactory.getLogger(IPv4AccessControl.class);
    public IPv4Ranges allow;
    public IPv4Ranges deny;

    public void validate(String clientIP) {
        try {
            InetAddress address = InetAddress.getByName(clientIP);
            if (isLocal(address)) {
                logger.debug("allow site local client address");
                return;
            }

            if (!allow(address.getAddress())) {
                throw new ForbiddenException("access denied", "IP_ACCESS_DENIED");
            }
        } catch (UnknownHostException e) {
            throw new Error(e);  // client ip format is already validated in ClientIPParser, so here it won't resolve DNS
        }
    }

    boolean isLocal(InetAddress address) {
        return address.isLoopbackAddress() || address.isSiteLocalAddress();
    }

    boolean allow(byte[] address) {
        if (address.length > 4) {   // only support ipv4, as Cloud LB generally uses ipv4 endpoint (gcloud supports both ipv4/v6, but ipv6 is not majority yet)
            logger.debug("skip with ipv6 client address");
            return true;
        }

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
