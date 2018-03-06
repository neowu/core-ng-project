package core.framework.impl.web.http;

import core.framework.util.Exceptions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author neo
 */
// refer to https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
// refer to https://github.com/spring-projects/spring-security/blob/master/web/src/main/java/org/springframework/security/web/util/matcher/IpAddressMatcher.java
class CIDR {
    static byte[] address(String address) {
        try {
            return InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    private final String cidr;
    private final byte[] address;
    private final byte[] mask;

    CIDR(String cidr) {
        this.cidr = cidr;
        int index = cidr.indexOf('/');
        if (index <= 0 || index >= cidr.length() - 1) throw Exceptions.error("invalid cidr, value={}", cidr);
        address = address(cidr.substring(0, index));
        int maskBits = Integer.parseInt(cidr.substring(index + 1));
        this.mask = mask(maskBits);
    }

    // generate mask with prefix length, e.g. prefix length = 9 => 11111111 10000000 00000000 00000000
    private byte[] mask(int maskBits) {
        int oddBits = maskBits % 8;
        int maskBytes = maskBits / 8 + (oddBits == 0 ? 0 : 1);  // 1 byte = 8 bits, so any thing more than 8 bits, add one extra byte
        byte[] mask = new byte[maskBytes];
        Arrays.fill(mask, 0, mask.length, (byte) 0xFF);    // fill all with 1
        if (oddBits != 0) {     // generate last mask byte like 11100000 (if oddBits = 3)
            mask[mask.length - 1] = (byte) (((1 << oddBits) - 1) << (8 - oddBits));
        }
        return mask;
    }

    boolean matches(byte[] remoteAddress) {
        if (mask.length == 0) return true;

        if (remoteAddress.length != address.length) return false;
        for (int i = 0; i < mask.length; i++) {
            if ((address[i] & mask[i]) != (remoteAddress[i] & mask[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return cidr;
    }
}
