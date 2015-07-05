package core.framework.impl.web;

import core.framework.api.util.Strings;

/**
 * @author neo
 */
class RemoteAddress {
    private final String remoteAddress;
    private final String xForwardedFor; // for original ip if there is proxy

    RemoteAddress(String remoteAddress, String xForwardedFor) {
        this.remoteAddress = remoteAddress;
        this.xForwardedFor = xForwardedFor;
    }

    /**
     * get actual client ip, being aware of proxy
     *
     * @return the ip of client from request
     */
    String clientIP() {
        if (Strings.empty(xForwardedFor))
            return remoteAddress;
        int index = xForwardedFor.indexOf(',');
        if (index > 0)
            return xForwardedFor.substring(0, index);
        return xForwardedFor;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!Strings.empty(xForwardedFor)) {
            builder.append(xForwardedFor).append(", ");
        }
        builder.append(remoteAddress);
        return builder.toString();
    }
}
