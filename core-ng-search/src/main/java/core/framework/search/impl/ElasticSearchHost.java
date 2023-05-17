package core.framework.search.impl;

import core.framework.util.Strings;
import org.apache.http.HttpHost;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author neo
 */
public final class ElasticSearchHost {
    public static HttpHost[] parse(String host) {
        String[] values = Strings.split(host, ',');
        HttpHost[] hosts = new HttpHost[values.length];
        for (int i = 0; i < values.length; i++) {
            String value = values[i].strip();
            hosts[i] = host(value);
        }
        return hosts;
    }

    private static HttpHost host(String value) {
        try {
            URL url = new URL(value);
            return new HttpHost(url.getHost(), 9200, url.getProtocol());
        } catch (MalformedURLException e) {
            return new HttpHost(value, 9200);
        }
    }

    private ElasticSearchHost() {
    }
}
