package core.framework.impl.web;

import core.framework.api.util.StopWatch;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.site.SiteManager;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class HTTPServer {
    static {
        // make undertow to use slf4j
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    public final SiteManager siteManager = new SiteManager();
    public final HTTPServerHandler handler;
    public int port = 8080;

    public HTTPServer(LogManager logManager) {
        handler = new HTTPServerHandler(logManager, siteManager);
    }

    public void start() {
        StopWatch watch = new StopWatch();
        try {
            Undertow server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(handler)
                .build();
            server.start();
        } finally {
            logger.info("http server started, elapsedTime={}", watch.elapsedTime());
        }
    }
}
