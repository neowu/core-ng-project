package core.framework.internal.web.request;

import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;

/**
 * @author neo
 */
public class ClientIPParser {
    // for common scenarios, e.g. Google LB(appends 2 ips)->kube service, AWS->nginx->webapp,
    // for google lb, it appends <immediate client IP>, <global forwarding rule external IP>, refer to https://cloud.google.com/load-balancing/docs/https#target-proxies
    public int maxForwardedIPs = 2;

    String parse(String remoteAddress, String xForwardedFor) {
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
    boolean hasMoreThanMaxForwardedIPs(String xForwardedFor) {
        if (xForwardedFor == null) return false;    // maxForwardedIPs must greater than 0
        int foundIPs = 1;
        int length = xForwardedFor.length();
        for (int i = 0; i < length; i++) {
            if (xForwardedFor.charAt(i) == ',') foundIPs++;
        }
        return foundIPs > maxForwardedIPs;
    }

    // check loosely to avoid unnecessary overhead, especially x-forwarded-for is extracted from right to left, where values are from trusted LB
    // ipv4 must have 3 dots and 1 optional colon, with hex chars
    // ipv6 must have only colons with hex chars
    String extractIP(String node) {
        int length = node.length();
        int dots = 0;
        int lastDotIndex = -1;
        int colons = 0;
        int lastColonIndex = -1;
        for (int i = 0; i < length; i++) {
            char ch = node.charAt(i);
            if (ch == '.') {
                dots++;
                lastDotIndex = i;
            } else if (ch == ':') {
                colons++;
                lastColonIndex = i;
            } else if (Character.digit(ch, 16) == -1) {
                throw new BadRequestException("invalid client ip address");
            }
        }
        if (dots == 0) return node; // should be ipv6 format
        if (dots == 3 && (colons == 0
                          || colons == 1 && lastColonIndex > lastDotIndex && lastColonIndex < length - 1)) {
            if (lastColonIndex > 0) return node.substring(0, lastColonIndex);   // should be ipv4:port
            return node;    // should be ipv4
        }
        throw new BadRequestException("invalid client ip address");
    }
}
