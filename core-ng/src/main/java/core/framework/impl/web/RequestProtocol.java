package core.framework.impl.web;

/**
 * @author neo
 */
public class RequestProtocol {
    private final String requestScheme;
    private final String xForwardedProto;
    private final int hostPort;
    private final String xForwardedPort;

    public RequestProtocol(String requestScheme, String xForwardedProto, int hostPort, String xForwardedPort) {
        this.requestScheme = requestScheme;
        this.xForwardedProto = xForwardedProto;
        this.hostPort = hostPort;
        this.xForwardedPort = xForwardedPort;
    }

    public String scheme() {
        return xForwardedProto != null ? xForwardedProto : requestScheme;
    }

    public int port() {
        if (xForwardedPort != null) {
            int index = xForwardedPort.indexOf(',');
            if (index > 0)
                return Integer.parseInt(xForwardedPort.substring(0, index));
            else
                return Integer.parseInt(xForwardedPort);
        }
        return hostPort;
    }
}
