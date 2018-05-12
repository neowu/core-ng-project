package core.framework.impl.web;

import core.framework.impl.log.LogManager;
import core.framework.impl.web.site.SiteManager;
import core.framework.util.StopWatch;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
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
    public boolean gzip;
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

            builder.setHandler(handler())
                   .setServerOption(UndertowOptions.DECODE_URL, false)
                   .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                   .setServerOption(UndertowOptions.ENABLE_RFC6265_COOKIE_VALIDATION, true)
                   .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, 10L * 1024 * 1024);  // max post body is 10M

            server = builder.build();
            server.start();
        } finally {
            logger.info("http server started, httpPort={}, httpsPort={}, gzip={}, elapsedTime={}", httpPort, httpsPort, gzip, watch.elapsedTime());
        }
    }

    private HttpHandler handler() {
        HTTPServerIOHandler handler = new HTTPServerIOHandler(this.handler);

        if (!gzip) return handler;

        GZipPredicate predicate = new GZipPredicate();
        return new EncodingHandler(new ContentEncodingRepository()
                .addEncodingHandler("gzip", new GzipEncodingProvider(), 100, predicate)
                .addEncodingHandler("deflate", new DeflateEncodingProvider(), 10, predicate))
                .setNext(handler);
    }

    public void stop() {
        if (server != null) {
            logger.info("stop http server");
            server.stop();
        }
    }
}
