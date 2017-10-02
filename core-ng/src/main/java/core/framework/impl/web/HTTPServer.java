package core.framework.impl.web;

import core.framework.api.util.StopWatch;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.site.SiteManager;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
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
    public Integer httpPort;
    public Integer httpsPort;
    private Undertow server;

    public HTTPServer(LogManager logManager) {
        handler = new HTTPServerHandler(logManager, siteManager);
    }

    public void start() {
        if (httpPort == null && httpsPort == null) {
            httpPort = 8080;    // by default start http
        }

        StopWatch watch = new StopWatch();
        try {
            Undertow.Builder builder = Undertow.builder();
            if (httpPort != null) builder.addHttpListener(httpPort, "0.0.0.0");
            if (httpsPort != null) builder.addHttpsListener(httpsPort, "0.0.0.0", new SSLContextBuilder().build());
            builder.setHandler(new HTTPServerIOHandler(handler))
                   .setServerOption(UndertowOptions.DECODE_URL, false)
                   .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                   .setServerOption(UndertowOptions.ENABLE_RFC6265_COOKIE_VALIDATION, true);
            server = builder.build();
            server.start();
        } finally {
            logger.info("http server started, httpPort={}, httpsPort={}, elapsedTime={}", httpPort, httpsPort, watch.elapsedTime());
        }
    }

    public void stop() {
        if (server != null) {
            logger.info("stop http server");
            server.stop();
        }
    }
}
