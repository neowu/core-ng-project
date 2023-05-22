package core.framework.search.impl;

import core.framework.util.Strings;
import org.apache.http.HttpHost;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author neo
 */
public final class ElasticSearchHost {
    public static HttpHost[] parse(String host) {
        String[] values = Strings.split(host, ',');
        var hosts = new HttpHost[values.length];
        for (int i = 0; i < values.length; i++) {
            String value = values[i].strip();
            hosts[i] = host(value);
        }
        return hosts;
    }

    private static HttpHost host(String value) {
        try {
            var uri = new URI(value);
            String host = uri.getHost();
            if (host == null) throw new Error("invalid elasticsearch host, host=" + value);
            int port = uri.getPort();
            if (port < 0) port = 9200;
            return new HttpHost(host, port, uri.getScheme());
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
    }
}
