package core.framework.impl.web.request;

import core.framework.util.Strings;

/**
 * @author neo
 */
public class ClientIPParser {
    public int maxForwardedIPs = 2;     // for common scenarios, e.g. Google LB(append 2 ips)->kube service, AWS->nginx->webapp

    String parse(String remoteAddress, String xForwardedFor) {
        if (Strings.isEmpty(xForwardedFor)) return remoteAddress;

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
        return xForwardedFor.substring(start, end).trim();
    }
}
