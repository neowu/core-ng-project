package core.framework.impl.web.management;

import core.framework.api.web.exception.ForbiddenException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author neo
 */
final class ControllerHelper {
    static void validateFromLocalNetwork(String clientIP) {
        try {
            InetAddress address = InetAddress.getByName(clientIP);
            if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()) {
                throw new ForbiddenException("access denied, clientIP=" + clientIP);
            }
        } catch (UnknownHostException e) {
            throw new ForbiddenException(e.getMessage(), ForbiddenException.DEFAULT_ERROR_CODE, e);
        }
    }
}
