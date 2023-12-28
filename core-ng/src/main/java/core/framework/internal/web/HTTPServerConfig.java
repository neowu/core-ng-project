package core.framework.internal.web;

import core.framework.util.Lists;
import core.framework.web.Interceptor;

import java.util.List;

/**
 * @author neo
 */
public class HTTPServerConfig {
    public final List<Interceptor> interceptors = Lists.newArrayList();
    public HTTPHost httpHost;
    public HTTPHost httpsHost;
    public boolean gzip;
    public long maxEntitySize = 10_000_000;    // limit max post body to 10M, apply to multipart as well

    public HTTPHost httpsHost() {
        if (httpHost == null && httpsHost == null) return new HTTPHost("0.0.0.0", 8443);    // by default start https only
        return httpsHost;
    }
}
