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
}
