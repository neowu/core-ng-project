package core.framework.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author neo
 */
public final class Network {
    private static String localHostAddress;

    public static String localHostAddress() {
        if (localHostAddress == null)
            try {
                String localHostName = InetAddress.getLocalHost().getHostAddress();
                Network.localHostAddress = localHostName;
                return localHostName;   // return temporary variable for lock free
            } catch (UnknownHostException e) {
                throw new Error(e);
            }
        return localHostAddress;
    }

    public static boolean isLocalAddress(String clientIP) {
        try {
            InetAddress address = InetAddress.getByName(clientIP);
            return address.isLoopbackAddress() || address.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }
}
