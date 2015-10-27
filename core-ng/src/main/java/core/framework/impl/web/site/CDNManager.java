package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.CDNFunction;

/**
 * @author neo
 */
public class CDNManager implements CDNFunction {
    String[] hosts;
    String version;

    @Override
    public String url(String url) {
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) return url;

        if (!url.startsWith("/")) throw Exceptions.error("url must start with '/', url={}", url);

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