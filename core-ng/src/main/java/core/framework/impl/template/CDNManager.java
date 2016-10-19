package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class CDNManager {
    private final Logger logger = LoggerFactory.getLogger(CDNManager.class);
    private String host;

    public String url(String url) {
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) return url;
        if (!url.startsWith("/")) throw Exceptions.error("url must start with '/', url={}", url);

        if (host == null) return url;
        return "//" + host + url;
    }

    public void host(String host) {
        logger.info("set cdn host, host={}", host);
        this.host = host;
    }
}
