package core.framework.impl.queue;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author neo
 */
public class Network {
    private static String localHostAddress;

    static String localHostAddress() {
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
