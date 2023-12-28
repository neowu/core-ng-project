package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import okhttp3.Request;
import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.StreamResetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RetryInterceptorTest {
    private RetryInterceptor interceptor;

    @BeforeEach
    void createRetryInterceptor() {
        interceptor = new RetryInterceptor(3, Duration.ofMillis(500));
    }

    /*
    java.net.SocketTimeoutException: connect timed out
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
    /*
    java.net.SocketException: Connection or outbound has closed
        at java.base/sun.security.ssl.SSLSocketImpl$AppOutputStream.write(Unknown Source)
        at okio.OutputStreamSink.write(JvmOkio.kt:53)
        at okio.AsyncTimeout$sink$1.write(AsyncTimeout.kt:103)
        at okio.RealBufferedSink.flush(RealBufferedSink.kt:267)
        at okhttp3.internal.http2.Http2Writer.flush(Http2Writer.kt:120)
        at okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:268)
        at okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:225)
        at okhttp3.internal.http2.Http2ExchangeCodec.writeRequestHeaders(Http2ExchangeCodec.kt:76)
        at okhttp3.internal.connection.Exchange.writeRequestHeaders(Exchange.kt:59)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:36)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:18)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:42)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.TimeoutInterceptor.intercept(TimeoutInterceptor.java:29)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
        at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:57)
        at core.framework.internal.web.service.WebServiceClient.execute(WebServiceClient.java:76)
    */
    /*
    okhttp3.internal.http2.ConnectionShutdownException
        at okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:246)
        at okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:225)
        at okhttp3.internal.http2.Http2ExchangeCodec.writeRequestHeaders(Http2ExchangeCodec.kt:76)
        at okhttp3.internal.connection.Exchange.writeRequestHeaders(Exchange.kt:59)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:36)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:18)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:42)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.TimeoutInterceptor.intercept(TimeoutInterceptor.java:29)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
        at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:57)
    */
    @Test
    void shouldRetryWithConnectionFailure() {
        assertThat(interceptor.shouldRetry(false, "GET", 2, new ConnectException("connection failed"))).isTrue();
        assertThat(interceptor.shouldRetry(false, "GET", 3, new ConnectException("connection failed"))).isFalse();

        assertThat(interceptor.shouldRetry(false, "POST", 1, new HttpConnectTimeoutException("connection timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(false, "POST", 1, new SocketTimeoutException("connect timed out"))).isTrue();
        assertThat(interceptor.shouldRetry(false, "POST", 1, new SocketException("Connection or outbound has closed"))).isTrue();
        assertThat(interceptor.shouldRetry(false, "POST", 1, new ConnectionShutdownException())).isTrue();
    }

    /*
    java.net.SocketException: Broken pipe
        at java.base/sun.nio.ch.NioSocketImpl.implWrite(Unknown Source)
        at java.base/sun.nio.ch.NioSocketImpl.write(Unknown Source)
        at java.base/sun.nio.ch.NioSocketImpl$2.write(Unknown Source)
        at java.base/java.net.Socket$SocketOutputStream.write(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketOutputRecord.deliver(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketImpl$AppOutputStream.write(Unknown Source)
        at okio.OutputStreamSink.write(JvmOkio.kt:53)
        at okio.AsyncTimeout$sink$1.write(AsyncTimeout.kt:103)
        at okio.RealBufferedSink.flush(RealBufferedSink.kt:267)
        at okhttp3.internal.http2.Http2Writer.flush(Http2Writer.kt:120)
        at okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:268)
        at okhttp3.internal.http2.Http2Connection.newStream(Http2Connection.kt:225)
        at okhttp3.internal.http2.Http2ExchangeCodec.writeRequestHeaders(Http2ExchangeCodec.kt:76)
        at okhttp3.internal.connection.Exchange.writeRequestHeaders(Exchange.kt:59)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:36)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:18)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:42)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.TimeoutInterceptor.intercept(TimeoutInterceptor.java:29)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
        at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:57)
    */
    /*
    java.net.SocketException: Connection reset by peer
        at java.base/sun.nio.ch.NioSocketImpl.implWrite(Unknown Source)
        at java.base/sun.nio.ch.NioSocketImpl.write(Unknown Source)
        at java.base/sun.nio.ch.NioSocketImpl$2.write(Unknown Source)
        at java.base/java.net.Socket$SocketOutputStream.write(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketOutputRecord.deliver(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketImpl$AppOutputStream.write(Unknown Source)
        at okio.OutputStreamSink.write(JvmOkio.kt:53)
        at okio.AsyncTimeout$sink$1.write(AsyncTimeout.kt:103)
        at okio.RealBufferedSink.flush(RealBufferedSink.kt:267)
        at okhttp3.internal.http2.Http2Writer.flush(Http2Writer.kt:120)
        at okhttp3.internal.http2.Http2Connection.flush(Http2Connection.kt:408)
        at okhttp3.internal.http2.Http2Stream$FramingSink.close(Http2Stream.kt:626)
        at okio.ForwardingSink.close(ForwardingSink.kt:37)
        at okhttp3.internal.connection.Exchange$RequestBodySink.close(Exchange.kt:242)
        at okio.RealBufferedSink.close(RealBufferedSink.kt:286)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:60)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:18)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:42)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.TimeoutInterceptor.intercept(TimeoutInterceptor.java:29)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
        at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:57)
    */
    @Test
    void shouldRetryWithBrokenWrite() {
        assertThat(interceptor.shouldRetry(false, "POST", 1, new SocketException("Broken pipe"))).isTrue();

        assertThat(interceptor.shouldRetry(false, "POST", 1, new SocketException("Connection reset by peer"))).isTrue();
    }

    /*
    java.net.ConnectException: Failed to connect to localhost/127.0.0.1:8443
        at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.kt:297)
        at okhttp3.internal.connection.RealConnection.connect(RealConnection.kt:207)
        at okhttp3.internal.connection.ExchangeFinder.findConnection(ExchangeFinder.kt:226)
        at okhttp3.internal.connection.ExchangeFinder.findHealthyConnection(ExchangeFinder.kt:106)
        at okhttp3.internal.connection.ExchangeFinder.find(ExchangeFinder.kt:74)
        at okhttp3.internal.connection.RealCall.initExchange$okhttp(RealCall.kt:255)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:32)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:42)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.TimeoutInterceptor.intercept(TimeoutInterceptor.java:29)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
        at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:57)
    Caused by: java.net.ConnectException: Connection refused
        at java.base/sun.nio.ch.Net.pollConnect(Native Method)
        at java.base/sun.nio.ch.Net.pollConnectNow(Net.java:669)
        at java.base/sun.nio.ch.NioSocketImpl.timedFinishConnect(NioSocketImpl.java:549)
        at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:597)
        at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:333)
        at java.base/java.net.Socket.connect(Socket.java:645)
        at okhttp3.internal.platform.Platform.connectSocket(Platform.kt:120)
        at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.kt:295)
     */
    @Test
    void shouldRetryWithConnectionRefused() {
        assertThat(interceptor.shouldRetry(false, "POST", 1, new ConnectException("Failed to connect to localhost/127.0.0.1:8443"))).isTrue();
    }

    /* with old eBPF/DataplaneV2 impl, service may reset connection during rolling update
    javax.net.ssl.SSLException: Connection reset
        at java.base/sun.security.ssl.Alert.createSSLException(Unknown Source)
        at java.base/sun.security.ssl.TransportContext.fatal(Unknown Source)
        at java.base/sun.security.ssl.TransportContext.fatal(Unknown Source)
        at java.base/sun.security.ssl.TransportContext.fatal(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketImpl.handleException(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketImpl$AppInputStream.read(Unknown Source)
        at okio.InputStreamSource.read(JvmOkio.kt:90)
        at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:129)
        at okio.RealBufferedSource.request(RealBufferedSource.kt:206)
        at okio.RealBufferedSource.require(RealBufferedSource.kt:199)
        at okhttp3.internal.http2.Http2Reader.nextFrame(Http2Reader.kt:89)
        at okhttp3.internal.http2.Http2Connection$ReaderRunnable.invoke(Http2Connection.kt:618)
        at okhttp3.internal.http2.Http2Connection$ReaderRunnable.invoke(Http2Connection.kt:609)
        at okhttp3.internal.concurrent.TaskQueue$execute$1.runOnce(TaskQueue.kt:98)
        at okhttp3.internal.concurrent.TaskRunner.runTask(TaskRunner.kt:116)
        at okhttp3.internal.concurrent.TaskRunner.access$runTask(TaskRunner.kt:42)
        at okhttp3.internal.concurrent.TaskRunner$runnable$1.run(TaskRunner.kt:65)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
        at java.base/java.lang.Thread.run(Unknown Source)
        Suppressed: java.net.SocketException: Broken pipe
            at java.base/sun.nio.ch.NioSocketImpl.implWrite(Unknown Source)
            at java.base/sun.nio.ch.NioSocketImpl.write(Unknown Source)
            at java.base/sun.nio.ch.NioSocketImpl$2.write(Unknown Source)
            at java.base/java.net.Socket$SocketOutputStream.write(Unknown Source)
            at java.base/sun.security.ssl.SSLSocketOutputRecord.encodeAlert(Unknown Source)
    Caused by: java.net.SocketException: Connection reset
        at java.base/sun.nio.ch.NioSocketImpl.implRead(Unknown Source)
        at java.base/sun.nio.ch.NioSocketImpl.read(Unknown Source)
        at java.base/sun.nio.ch.NioSocketImpl$1.read(Unknown Source)
        at java.base/java.net.Socket$SocketInputStream.read(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketInputRecord.read(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketInputRecord.readHeader(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketInputRecord.bytesInCompletePacket(Unknown Source)
        at java.base/sun.security.ssl.SSLSocketImpl.readApplicationRecord(Unknown Source)
    */
    /*
    okhttp3.internal.http2.StreamResetException: stream was reset: CANCEL
        at okhttp3.internal.http2.Http2Stream.takeHeaders(Http2Stream.kt:148)
        at okhttp3.internal.http2.Http2ExchangeCodec.readResponseHeaders(Http2ExchangeCodec.kt:96)
        at okhttp3.internal.connection.Exchange.readResponseHeaders(Exchange.kt:106)
        at okhttp3.internal.http.CallServerInterceptor.intercept(CallServerInterceptor.kt:79)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.ServiceUnavailableInterceptor.intercept(ServiceUnavailableInterceptor.java:18)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:34)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.kt:95)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.kt:83)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.kt:76)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.RetryInterceptor.intercept(RetryInterceptor.java:42)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at core.framework.internal.http.TimeoutInterceptor.intercept(TimeoutInterceptor.java:29)
        at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
        at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
        at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
        at core.framework.internal.http.HTTPClientImpl.execute(HTTPClientImpl.java:57)
    */
    @Test
    void shouldRetryWithConnectionReset() {
        assertThat(interceptor.shouldRetry(false, "GET", 1, new SSLException("Connection reset"))).isTrue();
        assertThat(interceptor.shouldRetry(false, "POST", 1, new SSLException("Connection reset"))).isFalse();

        assertThat(interceptor.shouldRetry(false, "POST", 1, new StreamResetException(ErrorCode.CANCEL))).isFalse();
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
    Caused by: java.net.SocketTimeoutException: Read timed out
        at java.base/java.net.SocketInputStream.socketRead0(Native Method)
        at java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:115)
        at java.base/java.net.SocketInputStream.read(SocketInputStream.java:168)
        at java.base/java.net.SocketInputStream.read(SocketInputStream.java:140)
        at okio.InputStreamSource.read(Okio.kt:102)
        at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:159)
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
    */
    @Test
    void shouldRetryWithReadTimeout() {
        // socket read timeout caught by AsyncTimeout
        var timeout = new SocketTimeoutException("timeout");
        timeout.initCause(new SocketTimeoutException("Read timed out"));
        assertThat(interceptor.shouldRetry(false, "POST", 1, timeout)).isFalse();
        assertThat(interceptor.shouldRetry(false, "PUT", 2, timeout)).isTrue();

        // okio AsyncTimeout close socket when timeout
        timeout = new SocketTimeoutException("timeout");
        timeout.initCause(new SocketException("Socket closed"));
        assertThat(interceptor.shouldRetry(false, "POST", 1, timeout)).isFalse();
        assertThat(interceptor.shouldRetry(false, "PUT", 2, timeout)).isTrue();
    }

    @Test
    void shouldRetryWithCallTimeout() {
        // okio AsyncTimout cancels call when if call timout
        var exception = new ConnectException("connection failed");
        assertThat(interceptor.shouldRetry(true, "POST", 1, exception)).isFalse();
        assertThat(interceptor.shouldRetry(true, "PUT", 2, exception)).isFalse();
    }

    @Test
    void shouldRetryWithUnknownHost() {
        // if failed to query DNS
        assertThat(interceptor.shouldRetry(false, "POST", 1, new UnknownHostException("unknown.host: System error"))).isTrue();
    }

    @Test
    void shouldRetryWithServiceUnavailable() {
        assertThat(interceptor.shouldRetry(HTTPStatus.OK.code, 1)).isFalse();
        assertThat(interceptor.shouldRetry(HTTPStatus.SERVICE_UNAVAILABLE.code, 1)).isTrue();
        assertThat(interceptor.shouldRetry(HTTPStatus.SERVICE_UNAVAILABLE.code, 3)).isFalse();
    }

    @Test
    void shouldRetryWithTooManyRequest() {
        assertThat(interceptor.shouldRetry(HTTPStatus.TOO_MANY_REQUESTS.code, 1)).isTrue();
        assertThat(interceptor.shouldRetry(HTTPStatus.TOO_MANY_REQUESTS.code, 3)).isFalse();
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

        actionLog.warningContext.maxProcessTimeInNano(Duration.ofSeconds(1).toNanos());
        assertThat(interceptor.withinMaxProcessTime(1)).isTrue();
        assertThat(interceptor.withinMaxProcessTime(2)).isFalse();

        logManager.end("end");
    }
}
