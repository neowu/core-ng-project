package core.framework.impl.queue;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author neo
 */
public class Network {
    private static String localHostName;

    static String localHostName() {
        if (localHostName == null)
            try {
                String localHostName = InetAddress.getLocalHost().getCanonicalHostName();
                Network.localHostName = localHostName;
                return localHostName;   // return temporary variable for lock free
            } catch (UnknownHostException e) {
                throw new Error(e);
            }
        return localHostName;
    }
}
