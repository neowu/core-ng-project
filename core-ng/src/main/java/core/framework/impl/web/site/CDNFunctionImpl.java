package core.framework.impl.web.site;

import core.framework.impl.template.CDNFunction;

/**
 * @author neo
 */
public class CDNFunctionImpl implements CDNFunction {
    String[] hosts;
    String version;

    @Override
    public String url(String url) {
        int hash = url.hashCode();
        int hostIndex = hash % hosts.length;
        StringBuilder builder = new StringBuilder(url.length() + 50);
        builder.append("//").append(hosts[hostIndex]).append(url);
        if (version != null) {
            if (url.contains("?")) builder.append("&v=");
            else builder.append("?v=");
            builder.append(version);
        }
        return builder.toString();
    }
}