package core.framework.http;

import core.framework.internal.http.DefaultTrustManager;
import core.framework.internal.http.HTTPClientImpl;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;

/**
 * @author neo
 */
public final class HTTPClientBuilder {
    static {
        // the system property must be set before loading jdk http client class, JDK uses static field to initialize those values

        // allow server ssl cert change during renegotiation
        // http client uses pooled connection, multiple requests to same host may hit different server behind LB
        // refer to sun.security.ssl.ClientHandshakeContext, allowUnsafeServerCertChange = Utilities.getBooleanProperty("jdk.tls.allowUnsafeServerCertChange", false);
        System.setProperty("jdk.tls.allowUnsafeServerCertChange", "true");

        // api client keep alive should be shorter than server side in case server side disconnect connection first
        // refer to jdk.internal.net.http.ConnectionPool,
        // jdk.internal.net.http.HttpClientImpl.SelectorManager.DEF_NODEADLINE uses 3s as interval to check
        System.setProperty("jdk.httpclient.keepalive.timeout", "30");   // 30s timeout for keep alive
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPClientBuilder.class);

    private Duration connectTimeout = Duration.ofSeconds(25);
    private Duration timeout = Duration.ofSeconds(60);
    private Duration slowOperationThreshold = Duration.ofSeconds(30);   // slow threshold should be greater than connect timeout, to let connect timeout happen first
    private boolean enableCookie = false;
    private boolean enableRedirect = false;
    private int maxRetries = 1;
    private String userAgent = "HTTPClient";
    private boolean trustAll = false;

    public HTTPClient build() {
        var watch = new StopWatch();
        try {
            HttpClient.Builder builder = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);

            if (trustAll) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
                builder.sslContext(sslContext);
            }

            builder.connectTimeout(connectTimeout);
            if (enableRedirect) builder.followRedirects(HttpClient.Redirect.NORMAL);
            if (enableCookie) builder.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

            return new HTTPClientImpl(builder.build(), userAgent, timeout, maxRetries, slowOperationThreshold);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new Error(e);
        } finally {
            logger.info("create http client, elapsed={}", watch.elapsed());
        }
    }

    public HTTPClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HTTPClientBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public HTTPClientBuilder slowOperationThreshold(Duration slowOperationThreshold) {
        this.slowOperationThreshold = slowOperationThreshold;
        return this;
    }

    public HTTPClientBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public HTTPClientBuilder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public HTTPClientBuilder enableCookie() {
        enableCookie = true;
        return this;
    }

    public HTTPClientBuilder enableRedirect() {
        enableRedirect = true;
        return this;
    }

    public HTTPClientBuilder trustAll() {
        trustAll = true;
        return this;
    }
}
