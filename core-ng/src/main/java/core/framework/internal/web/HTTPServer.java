package core.framework.internal.web;

import core.framework.internal.log.LogManager;
import core.framework.internal.web.site.SiteManager;
import core.framework.util.StopWatch;
import core.framework.web.Interceptor;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;
import org.xnio.Xnio;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public class HTTPServer {
    static {
        System.setProperty("org.jboss.logging.provider", "slf4j");      // make undertow to use slf4j, refer to org.jboss.logging.LoggerProviders
        // disable jboss custom thread pool, to use java built in DefaultThreadPoolExecutor
        // they offer similar performance with typical setup, so to pick simpler one
        // refer to org.jboss.threads.EnhancedQueueExecutor.DISABLE_HINT
        System.setProperty("jboss.threads.eqe.disable", "true");
    }

    public final SiteManager siteManager = new SiteManager();
    public final HTTPHandler handler;
    final ShutdownHandler shutdownHandler = new ShutdownHandler();

    private final Logger logger = LoggerFactory.getLogger(HTTPServer.class);
    private Undertow server;

    public HTTPServer(LogManager logManager) {
        handler = new HTTPHandler(logManager, siteManager.sessionManager, siteManager.templateManager);
    }

    public void start(HTTPServerConfig config) {
        var watch = new StopWatch();
        handler.interceptors = config.interceptors.toArray(new Interceptor[0]);
        HTTPHost httpHost = config.httpHost;
        HTTPHost httpsHost = config.httpsHost();
        try {
            Undertow.Builder builder = Undertow.builder();
            if (httpHost != null) builder.addHttpListener(httpHost.port(), httpHost.host());
            if (httpsHost != null) builder.addHttpsListener(httpsHost.port(), httpsHost.host(), new SSLContextBuilder().build());

            builder.setHandler(handler(config))
                // undertow accepts incoming connection very quick, backlog is hard to be filled even under load test, this setting is more for DDOS protection
                // and not necessary under cloud env, here to set to match linux default value
                // to use larger value, it requires to update kernel accordingly, e.g. sysctl -w net.core.somaxconn=1024 && sysctl -w net.ipv4.tcp_max_syn_backlog=4096
                .setSocketOption(Options.BACKLOG, 1024)
                .setServerOption(UndertowOptions.DECODE_URL, Boolean.FALSE)
                .setServerOption(UndertowOptions.ENABLE_HTTP2, Boolean.TRUE)
                .setServerOption(UndertowOptions.ENABLE_RFC6265_COOKIE_VALIDATION, Boolean.TRUE)
                // since we don't use Expires or Last-Modified header, so it's not necessary to set Date header, for cache, prefer cache-control/max-age
                // refer to https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.18.1
                .setServerOption(UndertowOptions.ALWAYS_SET_DATE, Boolean.FALSE)
                .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, Boolean.FALSE)
                // set tcp idle timeout to 620s, by default AWS ALB uses 60s, GCloud LB uses 600s, since it is always deployed with LB, longer timeout doesn't hurt
                // refer to https://cloud.google.com/load-balancing/docs/https/#timeouts_and_retries
                // refer to https://docs.aws.amazon.com/elasticloadbalancing/latest/application/application-load-balancers.html#connection-idle-timeout
                .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 620_000)         // 620s
                .setServerOption(UndertowOptions.SHUTDOWN_TIMEOUT, 10_000)            // 10s
                .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, config.maxEntitySize)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, Boolean.TRUE);

            builder.setWorker(Xnio.getInstance().createWorkerBuilder()
                .setWorkerIoThreads(Math.max(Runtime.getRuntime().availableProcessors(), 2))
                .setExternalExecutorService(handler.worker)
                .build());

            server = builder.build();
            server.start();
        } finally {
            logger.info("http server started, http={}, https={}, gzip={}, elapsed={}", httpHost, httpsHost, config.gzip, watch.elapsed());
        }
    }

    private HttpHandler handler(HTTPServerConfig config) {
        HttpHandler handler = new HTTPIOHandler(this.handler, shutdownHandler, config.maxEntitySize);
        if (config.gzip) {
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
                logger.warn(errorCode("FAILED_TO_STOP"), "failed to wait active http requests to complete");
                handler.worker.shutdownNow();
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
