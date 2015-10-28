package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author neo
 */
public class CDNManager {
    private final Logger logger = LoggerFactory.getLogger(CDNManager.class);

    private String[] hosts;
    private String version;

    public String url(String url) {
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) return url;
        if (!url.startsWith("/")) throw Exceptions.error("url must start with '/', url={}", url);

        if (hosts == null) return url;

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

    public void hosts(String... hosts) {
        logger.info("set cdn hosts, hosts={}", Arrays.toString(hosts));
        this.hosts = hosts;
    }

    public void version(String version) {
        logger.info("set cdn version, version={}", version);
        this.version = version;
    }
}
