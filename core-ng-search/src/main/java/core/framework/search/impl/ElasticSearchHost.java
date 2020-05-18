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
            hosts[i] = new HttpHost(value, 9200);
        }
        return hosts;
    }
}
