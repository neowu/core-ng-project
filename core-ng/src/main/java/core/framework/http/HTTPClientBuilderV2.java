package core.framework.http;

import core.framework.internal.http.DefaultTrustManager;
import core.framework.internal.http.v2.CookieManager;
import core.framework.internal.http.v2.HTTPClientImplV2;
import core.framework.internal.http.v2.RetryInterceptor;
import core.framework.util.StopWatch;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class HTTPClientBuilderV2 {
    static {
        // the system property must be set before loading jdk http client class, JDK uses static field to initialize those values

        // allow server ssl cert change during renegotiation
        // http client uses pooled connection, multiple requests to same host may hit different server behind LB
        // refer to sun.security.ssl.ClientHandshakeContext, allowUnsafeServerCertChange = Utilities.getBooleanProperty("jdk.tls.allowUnsafeServerCertChange", false);
        System.setProperty("jdk.tls.allowUnsafeServerCertChange", "true");
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPClientBuilderV2.class);

    private Duration connectTimeout = Duration.ofSeconds(25);
    private Duration timeout = Duration.ofSeconds(60);
    private Duration slowOperationThreshold = Duration.ofSeconds(30);   // slow threshold should be greater than connect timeout, to let connect timeout happen first
    private boolean enableCookie = false;
    private String userAgent = "HTTPClient";
    private boolean trustAll = false;
    private Integer maxRetries;

    public HTTPClient build() {
        var watch = new StopWatch();
        try {
            var pool = new ConnectionPool(100, 30, TimeUnit.SECONDS);
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .connectionPool(pool);

            if (trustAll) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                var trustManager = new DefaultTrustManager();
                sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
                builder.hostnameVerifier((hostname, sslSession) -> true)
                       .sslSocketFactory(sslContext.getSocketFactory(), trustManager);
            }

            if (maxRetries != null)
                builder.addInterceptor(new RetryInterceptor(pool, maxRetries));

            if (enableCookie) {
                builder.cookieJar(new CookieManager());
            }

            return new HTTPClientImplV2(builder.build(), userAgent, slowOperationThreshold);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new Error(e);
        } finally {
            logger.info("create http client, elapsed={}", watch.elapsed());
        }
    }

    public HTTPClientBuilderV2 connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HTTPClientBuilderV2 timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public HTTPClientBuilderV2 slowOperationThreshold(Duration slowOperationThreshold) {
        this.slowOperationThreshold = slowOperationThreshold;
        return this;
    }

    public HTTPClientBuilderV2 userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public HTTPClientBuilderV2 maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public HTTPClientBuilderV2 enableCookie() {
        enableCookie = true;
        return this;
    }

    public HTTPClientBuilderV2 trustAll() {
        trustAll = true;
        return this;
    }
}
