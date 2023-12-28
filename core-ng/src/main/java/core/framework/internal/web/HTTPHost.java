package core.framework.internal.web;

/**
 * @author neo
 */
public record HTTPHost(String host, int port) {
    public static HTTPHost parse(String value) {
        int index = value.indexOf(':');
        if (index > 0) return new HTTPHost(value.substring(0, index), Integer.parseInt(value.substring(index + 1)));
        return new HTTPHost("0.0.0.0", Integer.parseInt(value));
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
