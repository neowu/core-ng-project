package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RetryInterceptorTest {
    private RetryInterceptor interceptor;

    @BeforeEach
    void createRetryInterceptor() {
        interceptor = new RetryInterceptor(3, Duration.ofMillis(500), null);
    }

    @Test
    void shouldRetryWithConnectionException() {
        assertThat(interceptor.shouldRetry(1, "GET", new HttpTimeoutException("read timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(2, "GET", new ConnectException("connection failed"))).isTrue();
        assertThat(interceptor.shouldRetry(3, "GET", new ConnectException("connection failed"))).isFalse();

        assertThat(interceptor.shouldRetry(1, "GET", new SSLException("Connection reset"))).isTrue();

        assertThat(interceptor.shouldRetry(1, "POST", new HttpConnectTimeoutException("connection timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(1, "POST", new ConnectException("connection refused"))).isTrue();

        /* connect timeout stack trace
        Caused by: java.net.SocketTimeoutException: connect timed out
            at java.base/java.net.PlainSocketImpl.socketConnect(Native Method)
            at java.base/java.net.AbstractPlainSocketImpl.doConnect(Unknown Source)
            at java.base/java.net.AbstractPlainSocketImpl.connectToAddress(Unknown Source)
            at java.base/java.net.AbstractPlainSocketImpl.connect(Unknown Source)
            at java.base/java.net.SocksSocketImpl.connect(Unknown Source)
            at java.base/java.net.Socket.connect(Unknown Source)
            at okhttp3.internal.platform.Platform.connectSocket(Platform.kt:127)
            at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.kt:268)
            at okhttp3.internal.connection.RealConnection.connect(RealConnection.kt:176)
            at okhttp3.internal.connection.ExchangeFinder.findConnection(ExchangeFinder.kt:236)
            at okhttp3.internal.connection.ExchangeFinder.findHealthyConnection(ExchangeFinder.kt:109)
        */
        assertThat(interceptor.shouldRetry(1, "POST", new SocketTimeoutException("connect timed out"))).isTrue();
    }

    /* Read timeout stack trace with http 1.1
    Caused by: java.net.SocketTimeoutException: timeout
        at okio.SocketAsyncTimeout.newTimeoutException(Okio.kt:159)
        at okio.AsyncTimeout.exit$jvm(AsyncTimeout.kt:203)
        at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:163)
        at okio.RealBufferedSource.indexOf(RealBufferedSource.kt:349)
        at okio.RealBufferedSource.readUtf8LineStrict(RealBufferedSource.kt:222)
        at okhttp3.internal.http1.Http1ExchangeCodec.readHeaderLine(Http1ExchangeCodec.kt:210)
        at okhttp3.internal.http1.Http1ExchangeCodec.readResponseHeaders(Http1ExchangeCodec.kt:181)
        at okhttp3.internal.connection.Exchange.readResponseHeaders(Exchange.kt:105)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:82)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:19)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:37)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:82)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:84)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:71)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:38)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at okhttp3.RealCall.getResponseWithInterceptorChain(RealCall.kt:184)
        at okhttp3.RealCall.execute(RealCall.kt:66)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:49)
        ... 2 more
    Caused by: java.net.SocketTimeoutException: Read timed out
        at java.base/java.net.SocketInputStream.socketRead0(Native Method)
        at java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:115)
        at java.base/java.net.SocketInputStream.read(SocketInputStream.java:168)
        at java.base/java.net.SocketInputStream.read(SocketInputStream.java:140)
        at okio.InputStreamSource.read(Okio.kt:102)
        at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:159)
        ... 29 more
     */
    /* Read timeout stack trace with http 2.0
    Caused by: java.net.SocketTimeoutException: timeout
        at okhttp3.internal.http2.Http2Stream$StreamTimeout.newTimeoutException(Http2Stream.kt:666)
        at okhttp3.internal.http2.Http2Stream$StreamTimeout.exitAndThrowIfTimedOut(Http2Stream.kt:675)
        at okhttp3.internal.http2.Http2Stream.takeHeaders(Http2Stream.kt:142)
        at okhttp3.internal.http2.Http2ExchangeCodec.readResponseHeaders(Http2ExchangeCodec.kt:99)
        at okhttp3.internal.connection.Exchange.readResponseHeaders(Exchange.kt:105)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:82)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:19)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:37)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:82)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:84)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:71)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:38)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:112)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:87)
        at okhttp3.RealCall.getResponseWithInterceptorChain(RealCall.kt:184)
        at okhttp3.RealCall.execute(RealCall.kt:66)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:49)
        ... 2 more
     */
    @Test
    void shouldRetryWithReadTimeout() {
        // socket read timeout caught by AsyncTimeout
        var timeout = new SocketTimeoutException("timeout");
        timeout.initCause(new SocketTimeoutException("Read timed out"));
        assertThat(interceptor.shouldRetry(1, "POST", timeout)).isFalse();
        assertThat(interceptor.shouldRetry(2, "PUT", timeout)).isTrue();

        timeout = new SocketTimeoutException("timeout");
        assertThat(interceptor.shouldRetry(1, "POST", timeout)).isFalse();
        assertThat(interceptor.shouldRetry(2, "PUT", timeout)).isTrue();

        // okio AsyncTimeout close socket when timeout
        timeout = new SocketTimeoutException("timeout");
        timeout.initCause(new SocketException("Socket closed"));
        assertThat(interceptor.shouldRetry(1, "POST", timeout)).isFalse();
        assertThat(interceptor.shouldRetry(2, "PUT", timeout)).isTrue();

        // okio AsyncTimout cancels call when if call timout
        var cancelled = new IOException("Canceled");
        assertThat(interceptor.shouldRetry(1, "POST", cancelled)).isFalse();
        assertThat(interceptor.shouldRetry(2, "PUT", cancelled)).isTrue();
    }

    @Test
    void shouldRetryWithUnknownHost() {
        // if failed to query DNS
        assertThat(interceptor.shouldRetry(1, "POST", new UnknownHostException("unknown.host: System error"))).isTrue();
    }

    @Test
    void shouldRetryWithServiceUnavailable() {
        assertThat(interceptor.shouldRetry(1, HTTPStatus.OK.code)).isFalse();
        assertThat(interceptor.shouldRetry(1, HTTPStatus.SERVICE_UNAVAILABLE.code)).isTrue();
        assertThat(interceptor.shouldRetry(3, HTTPStatus.SERVICE_UNAVAILABLE.code)).isFalse();
    }

    @Test
    void shouldRetryWithTooManyRequest() {
        assertThat(interceptor.shouldRetry(1, HTTPStatus.TOO_MANY_REQUESTS.code)).isTrue();
        assertThat(interceptor.shouldRetry(3, HTTPStatus.TOO_MANY_REQUESTS.code)).isFalse();
    }

    @Test
    void uri() {
        var request = new Request.Builder().url("http://localhost/path?query=value").build();
        assertThat(interceptor.uri(request)).isEqualTo("http://localhost/path");
    }

    @Test
    void withinMaxProcessTime() {
        var logManager = new LogManager();
        ActionLog actionLog = logManager.begin("begin", null);
        actionLog.maxProcessTimeInNano = Duration.ofSeconds(1).toNanos();

        assertThat(interceptor.withinMaxProcessTime(1)).isTrue();
        assertThat(interceptor.withinMaxProcessTime(2)).isFalse();

        logManager.end("end");
    }
}
