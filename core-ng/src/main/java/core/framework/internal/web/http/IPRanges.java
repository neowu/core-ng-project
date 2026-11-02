package core.framework.internal.web.http;

import java.net.InetAddress;

public interface IPRanges {
    static byte[] address(String address) {
        return InetAddress.ofLiteral(address).getAddress();    // only parse ip literal, never resolve DNS
    }

    boolean matches(byte[] address);
}
