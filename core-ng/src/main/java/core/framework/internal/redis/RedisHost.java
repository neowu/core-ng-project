package core.framework.internal.redis;

/**
 * @author neo
 */
public class RedisHost {
    static final int DEFAULT_PORT = 6379;
    public final String host;
    public final int port;

    public RedisHost(String host) {
        int index = host.indexOf(':');
        if (index == 0 || index == host.length() - 1) throw new Error("invalid host format, host=" + host);
        if (index != -1) {
            this.host = host.substring(0, index);
            try {
                port = Integer.parseInt(host.substring(index + 1));
            } catch (NumberFormatException e) {
                throw new Error("invalid host format, host=" + host, e);
            }
        } else {
            this.host = host;
            port = DEFAULT_PORT;
        }
    }

    @Override
    public String toString() {  // make it easier to log
        return port == DEFAULT_PORT ? host : host + ':' + port;
    }
}
