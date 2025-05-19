package core.framework.search.impl;

import core.framework.util.Strings;
import org.apache.http.HttpHost;

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
        int hostStart = 0;
        int hostEnd = value.length();
        String schema = "http";
        int schemaIndex = value.indexOf("://");
        if (schemaIndex > 0) {
            if (schemaIndex == value.length() - 3) throw new Error("invalid elasticsearch host, host=" + value);
            schema = value.substring(0, schemaIndex);
            hostStart = schemaIndex + 3;
        }
        int portIndex = value.indexOf(':', schemaIndex + 1);
        int port = 9200;
        if (portIndex > 0) {
            if (portIndex == value.length() - 1) throw new Error("invalid elasticsearch host, host=" + value);
            port = Integer.parseInt(value.substring(portIndex + 1));
            hostEnd = portIndex;
        }
        return new HttpHost(value.substring(hostStart, hostEnd), port, schema);
    }
}
