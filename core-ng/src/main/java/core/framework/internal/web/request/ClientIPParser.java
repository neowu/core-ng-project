package core.framework.internal.web.request;

import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;
import org.jspecify.annotations.Nullable;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * @author neo
 */
public class ClientIPParser {
    // for common scenarios, e.g. Google LB(appends 2 ips)->kube service, AWS->nginx->webapp,
    // for google lb, it appends <immediate client IP>, <global forwarding rule external IP>, refer to https://cloud.google.com/load-balancing/docs/https#target-proxies
    public int maxForwardedIPs = 2;

    String parse(String remoteAddress, @Nullable String xForwardedFor) {
        if (Strings.isBlank(xForwardedFor)) return remoteAddress;

        int foundForwardedIPs = 1;
        int index = xForwardedFor.length() - 1;
        int start;
        int end = index + 1;    // substring end index is exclusive
        while (true) {
            char ch = xForwardedFor.charAt(index);
            if (ch == ',') {
                foundForwardedIPs++;
                if (foundForwardedIPs > maxForwardedIPs) {
                    start = index + 1;
                    break;
                } else {
                    end = index;
                }
            }
            if (index == 0) {
                start = index;
                break;
            }
            index--;
        }

        // according to https://tools.ietf.org/html/rfc7239
        // x-forwarded-for = node, node, ...
        // node     = nodename [ ":" node-port ]
        // nodename = IPv4address / "[" IPv6address "]" / "unknown" / obfnode
        // currently only Azure Application Gateway may use ipv4:port, and it doesn't support ipv6 yet
        // so here only to support ipv4, ipv4:port, ipv6 format
        String node = xForwardedFor.substring(start, end).trim();
        return extractIP(node);
    }

    // could be x-forwarded-for spoofing or pass thru http forward proxies
    boolean hasMoreThanMaxForwardedIPs(@Nullable String xForwardedFor) {
        if (xForwardedFor == null) return false;    // maxForwardedIPs must greater than 0
        int foundIPs = 1;
        int length = xForwardedFor.length();
        for (int i = 0; i < length; i++) {
            if (xForwardedFor.charAt(i) == ',') foundIPs++;
        }
        return foundIPs > maxForwardedIPs;
    }

    // validate with InetAddress.ofLiteral, which only parses ip literal and never does DNS lookup, so downstream (e.g. IPAccessControl) always gets valid ip
    String extractIP(String node) {
        String ip = node;
        int colonIndex = node.indexOf(':');
        // only 1 colon means ipv4:port, ipv6 must have at least 2 colons
        if (colonIndex > 0 && colonIndex < node.length() - 1 && node.indexOf(':', colonIndex + 1) == -1) ip = node.substring(0, colonIndex);

        InetAddress address;
        try {
            address = InetAddress.ofLiteral(ip);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("invalid client ip address", "INVALID_HTTP_REQUEST", e);
        }
        // ofLiteral accepts legacy ipv4 forms, e.g. 1.2, 192.0.2, 010.1.1.1, ::ffff:1.2.3.4, only allow standard dotted-decimal
        if (address instanceof Inet4Address && !ip.equals(address.getHostAddress())) {
            throw new BadRequestException("invalid client ip address", "INVALID_HTTP_REQUEST");
        }
        return ip;
    }
}
