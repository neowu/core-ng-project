package core.framework.http;

import core.framework.impl.http.HTTPClientImpl;
import core.framework.impl.http.TrustAllTrustManager;
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
    private final Logger logger = LoggerFactory.getLogger(HTTPClientBuilder.class);

    private Duration timeout = Duration.ofSeconds(60);
    private Duration slowOperationThreshold = Duration.ofSeconds(30);
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
                sslContext.init(null, new TrustManager[]{new TrustAllTrustManager()}, new SecureRandom());
                builder.sslContext(sslContext);
            }

            builder.connectTimeout(timeout);
            if (enableRedirect) builder.followRedirects(HttpClient.Redirect.NORMAL);
            if (enableCookie) builder.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            HttpClient httpClient = builder.build();

            return new HTTPClientImpl(httpClient, userAgent, timeout, maxRetries, slowOperationThreshold);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new Error(e);
        } finally {
            logger.info("create http client, elapsed={}", watch.elapsed());
        }
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
