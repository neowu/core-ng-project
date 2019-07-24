package core.framework.impl.web.request;

import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;

/**
 * @author neo
 */
public class ClientIPParser {
    // for common scenarios, e.g. Google LB(appends 2 ips)->kube service, AWS->nginx->webapp,
    // for google lb, it appends <immediate client IP>, <global forwarding rule external IP>, refer to https://cloud.google.com/compute/docs/load-balancing/http/ (target proxies section)
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

        String clientIP = xForwardedFor.substring(start, end).trim();
        if (!isValidIP(clientIP)) throw new BadRequestException("invalid client ip address");
        return clientIP;
    }

    // only check loose format, ipv4 must have 3 dots, and each char must be hex, to cover both ipv6 and ipv4
    // currently since kube doesn't supports creating both ipv6/ipv4 forwarding rule yet, so we mainly use ipv4 only
    // will revise once we start to use ipv6
    boolean isValidIP(String clientIP) {
        int dots = 0;
        for (int i = 0; i < clientIP.length(); i++) {
            char ch = clientIP.charAt(i);
            if (ch == '.') {
                dots++;
            } else if (ch == ':') {
                if (dots > 0) return false; // colons must not appear after dots.
            } else if (Character.digit(ch, 16) == -1) {
                return false; // everything else must be a decimal or hex digit.
            }
        }

        return dots == 0 || dots == 3;
    }
}
