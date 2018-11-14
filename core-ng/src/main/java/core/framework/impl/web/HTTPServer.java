package core.framework.impl.web;

import core.framework.impl.log.LogManager;
import core.framework.impl.web.site.SiteManager;
import core.framework.util.StopWatch;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class HTTPServer {
    static {
        System.setProperty("org.jboss.logging.provider", "slf4j");  // make undertow to use slf4j
    }

    public final SiteManager siteManager = new SiteManager();
    public final HTTPHandler handler;
    private final Logger logger = LoggerFactory.getLogger(HTTPServer.class);
    private final ShutdownHandler shutdownHandler = new ShutdownHandler();
    public Integer httpPort;
    public Integer httpsPort;
    public boolean gzip;
    private Undertow server;

    public HTTPServer(LogManager logManager) {
        handler = new HTTPHandler(logManager, siteManager.sessionManager, siteManager.templateManager);
    }

    public void start() {
        if (httpPort == null && httpsPort == null) httpPort = 8080;    // by default start http

        var watch = new StopWatch();
        try {
            Undertow.Builder builder = Undertow.builder();
            if (httpPort != null) builder.addHttpListener(httpPort, "0.0.0.0");
            if (httpsPort != null) builder.addHttpsListener(httpsPort, "0.0.0.0", new SSLContextBuilder().build());

            builder.setHandler(handler())
                   .setServerOption(UndertowOptions.DECODE_URL, Boolean.FALSE)
                   .setServerOption(UndertowOptions.ENABLE_HTTP2, Boolean.TRUE)
                   .setServerOption(UndertowOptions.ENABLE_RFC6265_COOKIE_VALIDATION, Boolean.TRUE)
                   // since we don't use Expires or Last- Modified header, so it's not necessary to set Date header, for cache, prefer cache-control/max-age
                   // refer to https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.18.1
                   .setServerOption(UndertowOptions.ALWAYS_SET_DATE, Boolean.FALSE)
                   .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, Boolean.FALSE)
                   // set tcp idle timeout to 620s, by default AWS ALB uses 60s, GCloud LB uses 600s, since it is always deployed with LB, longer timeout doesn't hurt
                   // refer to https://cloud.google.com/load-balancing/docs/https/#timeouts_and_retries
                   // refer to https://docs.aws.amazon.com/elasticloadbalancing/latest/application/application-load-balancers.html#connection-idle-timeout
                   .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 620 * 1000)     // 620s
                   .setServerOption(UndertowOptions.SHUTDOWN_TIMEOUT, 10 * 1000)        // 10s
                   .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, 10L * 1024 * 1024);    // max post body is 10M

            server = builder.build();
            server.start();
        } finally {
            logger.info("http server started, httpPort={}, httpsPort={}, gzip={}, elapsed={}", httpPort, httpsPort, gzip, watch.elapsed());
        }
    }

    private HttpHandler handler() {
        HttpHandler handler = new HTTPIOHandler(this.handler, shutdownHandler);
        if (gzip) {
            // only support gzip, deflate is less popular
            handler = new EncodingHandler(handler, new ContentEncodingRepository()
                    .addEncodingHandler("gzip", new GzipEncodingProvider(), 100, new GZipPredicate()));
        }
        return handler;
    }

    public void shutdown() {
        if (server != null) {
            logger.info("shutting down http server");
            shutdownHandler.shutdown();
            if (handler.webSocketHandler != null)
                handler.webSocketHandler.shutdown();
        }
    }

    public void awaitRequestCompletion(long timeoutInMs) throws InterruptedException {
        if (server != null) {
            boolean success = shutdownHandler.awaitTermination(timeoutInMs);
            if (!success) {
                logger.warn("failed to wait active http requests to complete");
                server.getWorker().shutdownNow();
            } else {
                logger.info("active http requests completed");
            }
        }
    }

    public void awaitTermination() {
        if (server != null) {
            server.stop();
            logger.info("http server stopped");
        }
    }
}
