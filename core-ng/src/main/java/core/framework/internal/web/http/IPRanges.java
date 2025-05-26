package core.framework.internal.web.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface IPRanges {
    static byte[] address(String address) {
        try {
            return InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    boolean matches(byte[] address);
}
