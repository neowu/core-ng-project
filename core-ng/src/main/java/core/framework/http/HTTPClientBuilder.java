package core.framework.http;

import core.framework.internal.http.CookieManager;
import core.framework.internal.http.DefaultTrustManager;
import core.framework.internal.http.HTTPClientImpl;
import core.framework.internal.http.HTTPEventListenerFactory;
import core.framework.internal.http.PEM;
import core.framework.internal.http.RetryInterceptor;
import core.framework.internal.http.ServiceUnavailableInterceptor;
import core.framework.internal.http.TimeoutInterceptor;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import okhttp3.ConnectionPool;
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
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
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

        // refer to jdk InetAddressCachePolicy.class, it reads value from Security properties
        // for application to override, one way is to put Security.setProperty() in Main before starting app
        // override default to 300
        if (Security.getProperty("networkaddress.cache.ttl") == null) {
            Security.setProperty("networkaddress.cache.ttl", "300");
        }
        // override negative default from 10 to 0, not cache failure when http client retry dns resolving
        if ("10".equals(Security.getProperty("networkaddress.cache.negative.ttl"))) {
            Security.setProperty("networkaddress.cache.negative.ttl", "0");
        }
    }

    private final Logger logger = LoggerFactory.getLogger(HTTPClientBuilder.class);

    private Duration connectTimeout = Duration.ofSeconds(25);
    private Duration timeout = Duration.ofSeconds(60);
    private Duration keepAlive = Duration.ofSeconds(30);    // conservative setting, as http connection to internet/external can be cut off by NAT/firewall if idle too long
    private Duration slowOperationThreshold = Duration.ofSeconds(30);   // slow threshold should be longer than connect timeout
    private boolean enableCookie = false;
    private String userAgent = "HTTPClient";
    private boolean trustAll = false;
    private KeyStore trustStore;
    private Integer maxRetries;
    private Duration retryWaitTime = Duration.ofMillis(500);

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
                    .connectionPool(new ConnectionPool(100, keepAlive.toSeconds(), TimeUnit.SECONDS))
                    .eventListenerFactory(new HTTPEventListenerFactory());

            configureHTTPS(builder);

            builder.addInterceptor(new TimeoutInterceptor());
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

    // client level connect timeout, request level timeout can be specified in HTTPRequest
    public HTTPClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    // client level read/write timeout, request level timeout can be specified in HTTPRequest
    public HTTPClientBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public HTTPClientBuilder keepAlive(Duration keepAlive) {
        this.keepAlive = keepAlive;
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

                // add all system issuers to trust store to accept all authorized http certs
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore) null);
                for (TrustManager trustManager : factory.getTrustManagers()) {
                    if (trustManager instanceof X509TrustManager) {
                        for (X509Certificate issuer : ((X509TrustManager) trustManager).getAcceptedIssuers()) {
                            trustStore.setCertificateEntry(issuer.getSubjectDN().getName(), issuer);
                        }
                    }
                }
            }
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate certificate = factory.generateCertificate(new ByteArrayInputStream(PEM.decode(cert)));
            trustStore.setCertificateEntry(String.valueOf(trustStore.size() + 1), certificate);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
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
