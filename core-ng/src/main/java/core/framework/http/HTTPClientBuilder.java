package core.framework.http;

import core.framework.internal.http.CookieManager;
import core.framework.internal.http.DefaultTrustManager;
import core.framework.internal.http.FallbackDNSCache;
import core.framework.internal.http.HTTPClientImpl;
import core.framework.internal.http.HTTPEventListenerFactory;
import core.framework.internal.http.PEM;
import core.framework.internal.http.RetryInterceptor;
import core.framework.internal.http.ServiceUnavailableInterceptor;
import core.framework.internal.http.TimeoutInterceptor;
import core.framework.util.StopWatch;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
    // there is already networkaddress.cache.ttl=300s, this is to mitigate dns failure further,
    // like external domain can be under DDos attack constantly (DNS hijacking) or unstable DNS query between countries
    // this is trying to use previous success resolution to reduce intermittent dns resolve failures
    private boolean enableFallbackDNSCache = false;
    private String userAgent = "HTTPClient";
    private boolean trustAll = false;
    private KeyStore trustStore;
    private KeyManager[] keyManagers;   // for client auth
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
                .eventListenerFactory(new HTTPEventListenerFactory())
                .retryOnConnectionFailure(false)    // disable all okHTTP builtin retry and followups, those should be handled on application level for traces
                .followRedirects(false)
                .followSslRedirects(false);

            configureHTTPS(builder);

            builder.addInterceptor(new TimeoutInterceptor());
            if (maxRetries != null) {
                builder.addNetworkInterceptor(new ServiceUnavailableInterceptor());
                builder.addInterceptor(new RetryInterceptor(maxRetries, retryWaitTime));
            }
            if (enableCookie) builder.cookieJar(new CookieManager());
            if (enableFallbackDNSCache) builder.dns(new FallbackDNSCache());

            return new HTTPClientImpl(builder.build(), userAgent, slowOperationThreshold, timeout);
        } finally {
            logger.info("create http client, elapsed={}", watch.elapsed());
        }
    }

    private void configureHTTPS(OkHttpClient.Builder builder) {
        if (!trustAll && trustStore == null && keyManagers == null) return;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers;
            if (trustAll) {
                trustManagers = new TrustManager[]{new DefaultTrustManager()};
                builder.hostnameVerifier((hostname, sslSession) -> true);
            } else {
                var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            }
            sslContext.init(keyManagers, trustManagers, null);  // lgtm [java/insecure-trustmanager]
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
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

    public HTTPClientBuilder enableFallbackDNSCache() {
        enableFallbackDNSCache = true;
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
                    if (trustManager instanceof final X509TrustManager manager) {
                        for (X509Certificate issuer : manager.getAcceptedIssuers()) {
                            trustStore.setCertificateEntry(issuer.getSubjectX500Principal().getName(), issuer);
                        }
                    }
                }
            }
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate certificate = factory.generateCertificate(new ByteArrayInputStream(PEM.decode(cert)));
            trustStore.setCertificateEntry(String.valueOf(trustStore.size() + 1), certificate);
            return this;
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    public HTTPClientBuilder clientAuth(String privateKey, String cert) {
        try {
            PrivateKey clientPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(PEM.decode(privateKey)));
            Certificate clientCertificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(PEM.decode(cert)));

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("default", clientPrivateKey, new char[0], new Certificate[]{clientCertificate});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, new char[0]);
            keyManagers = keyManagerFactory.getKeyManagers();
            return this;
        } catch (UnrecoverableKeyException | CertificateException | InvalidKeySpecException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new Error(e);
        }
    }

    // this is rough estimate, like read timeout doesn't mean fetching full response will complete within duration of timeout
    Duration callTimeout() {
        int attempts = maxRetries == null ? 1 : maxRetries;
        long callTimeout = connectTimeout.toMillis() + timeout.toMillis() * attempts;
        for (int i = 1; i < attempts; i++) {
            callTimeout += retryWaitTime.toMillis() << i - 1;
        }
        callTimeout += 2000;  // add 2 seconds as extra buffer
        return Duration.ofMillis(callTimeout);
    }
}
