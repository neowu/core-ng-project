package core.framework.impl.web.site;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.function.Function;
import core.framework.impl.template.function.HTMLText;

/**
 * @author neo
 */
public class CDNFunction implements Function {
    String[] hosts;
    String version;

    @Override
    public Object apply(Object[] params) {
        String url = String.valueOf(params[0]);
        if (url.charAt(0) != '/') throw Exceptions.error("url must start with /, url={}", url);
        if (hosts == null) return new HTMLText(url);
        return new HTMLText(buildURL(url));
    }

    String buildURL(String url) {
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