package core.framework.search.impl;

import core.framework.util.Strings;
import org.apache.http.HttpHost;

/**
 * @author neo
 */
public final class ElasticSearchHost {
    public static HttpHost[] parse(String host) {
        String[] values = Strings.split(host, ',');
        HttpHost[] hosts = new HttpHost[values.length];
        for (int i = 0; i < values.length; i++) {
            String value = values[i].strip();
            hosts[i] = parseHostString(value);
        }
        return hosts;
    }

    // modified from HttpHost.create()
    private static HttpHost parseHostString(String host) {
        String text = host;
        String scheme = "http";
        int port = 9200;
        final int schemeIdx = text.indexOf("://");
        if (schemeIdx > 0) {
            scheme = text.substring(0, schemeIdx);
            text = text.substring(schemeIdx + 3);
        }
        final int portIdx = text.lastIndexOf(':');
        if (portIdx > 0) {
            try {
                port = Integer.parseInt(text.substring(portIdx + 1));
            } catch (final NumberFormatException ex) {
                throw new Error("invalid search host, host= " + host, ex);
            }
            text = text.substring(0, portIdx);
        }
        return new HttpHost(text, port, scheme);
    }
}
