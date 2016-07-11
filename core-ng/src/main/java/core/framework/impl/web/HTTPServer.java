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

    public final SiteManager siteManager = new SiteManager();
    public final HTTPServerHandler handler;
    private final Logger logger = LoggerFactory.getLogger(HTTPServer.class);
    public int port = 8080;
    private Undertow server;

    public HTTPServer(LogManager logManager) {
        handler = new HTTPServerHandler(logManager, siteManager);
    }

    public void start() {
        StopWatch watch = new StopWatch();
        try {
            server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(new HTTPServerIOHandler(handler))
                .build();
            server.start();
        } finally {
            logger.info("http server started, port={}, elapsedTime={}", port, watch.elapsedTime());
        }
    }

    public void stop() {
        if (server != null) {
            logger.info("stop http server");
            server.stop();
        }
    }
}
