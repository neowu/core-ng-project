package core.framework.impl.web.management;

import core.framework.util.Network;
import core.framework.web.exception.ForbiddenException;

/**
 * @author neo
 */
final class ControllerHelper {
    static void assertFromLocalNetwork(String clientIP) {
        if (!Network.isLocalAddress(clientIP)) {
            throw new ForbiddenException("access denied, clientIP=" + clientIP);
        }
    }
}
