package core.framework.http;

import core.framework.internal.http.CookieManager;
import core.framework.internal.http.DefaultTrustManager;
import core.framework.internal.http.HTTPClientImpl;
import core.framework.internal.http.PEM;
import core.framework.internal.http.RetryInterceptor;
import core.framework.internal.http.ServiceUnavailableInterceptor;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPClientBuilder.class);

    private Duration connectTimeout = Duration.ofSeconds(25);
    private Duration timeout = Duration.ofSeconds(60);
    private Duration slowOperationThreshold = Duration.ofSeconds(30);   // slow threshold should be greater than connect timeout, to let connect timeout happen first
    private boolean enableCookie = false;
    private String userAgent = "HTTPClient";
    private boolean trustAll = false;
    private KeyStore trustStore;
    private Integer maxRetries;
    private Duration retryWaitTime = Duration.ofMillis(500);
    private String[] tlsVersions;

    // force to use HTTPClient.builder()
    HTTPClientBuilder() {
    }

    public HTTPClient build() {
        var watch = new StopWatch();
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout)
                    .readTimeout(timeout)
                    .writeTimeout(timeout)
                    .callTimeout(callTimeout()) // call timeout is only used as last defense, timeout for complete call includes connect/retry/etc
                    .connectionPool(new ConnectionPool(100, 30, TimeUnit.SECONDS));

            configureHTTPS(builder);

            if (tlsVersions != null) {  // TLSv1.3 is troublesome, jdk impl is still not complete and may cause problem with different TLSv1.3 server, this is to provide way to disable client TLSv1.3
                builder.connectionSpecs(List.of(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(tlsVersions)
                        .build(), ConnectionSpec.CLEARTEXT));
            }

            if (maxRetries != null) {
                builder.addNetworkInterceptor(new ServiceUnavailableInterceptor());
                builder.addInterceptor(new RetryInterceptor(maxRetries, retryWaitTime, Threads::sleepRoughly));
            }
            if (enableCookie) builder.cookieJar(new CookieManager());

            return new HTTPClientImpl(builder.build(), userAgent, slowOperationThreshold);
        } finally {
            logger.info("create http client, elapsed={}", watch.elapsed());
        }
    }

    private void configureHTTPS(OkHttpClient.Builder builder) {
        if (!trustAll && trustStore == null) return;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager;
            if (trustAll) {
                trustManager = new DefaultTrustManager();
            } else {
                var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
            }
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            builder.hostnameVerifier((hostname, sslSession) -> true)
                   .sslSocketFactory(sslContext.getSocketFactory(), trustManager);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new Error(e);
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

    public HTTPClientBuilder retryWaitTime(Duration waitTime) {
        retryWaitTime = waitTime;
        return this;
    }

    public HTTPClientBuilder enableCookie() {
        enableCookie = true;
        return this;
    }

    public HTTPClientBuilder trustAll() {
        trustAll = true;
        return this;
    }

    public HTTPClientBuilder trust(String cert) {
        try {
            if (trustStore == null) {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);  // must be init first
            }
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate certificate = factory.generateCertificate(new ByteArrayInputStream(PEM.decode(cert)));
            trustStore.setCertificateEntry(String.valueOf(trustStore.size() + 1), certificate);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        return this;
    }

    public HTTPClientBuilder tlsVersions(String... versions) {
        tlsVersions = versions;
        return this;
    }

    Duration callTimeout() {
        int attempts = maxRetries == null ? 1 : maxRetries;
        long timeout = connectTimeout.toMillis() + this.timeout.toMillis() * attempts;
        for (int i = 1; i < attempts; i++) {
            timeout += 600 << i - 1;    // rough sleep can be +20% of 500ms
        }
        timeout += 2000;  // add 2 seconds as extra buffer
        return Duration.ofMillis(timeout);
    }
}
