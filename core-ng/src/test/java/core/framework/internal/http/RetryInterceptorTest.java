package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class RetryInterceptorTest {
    private RetryInterceptor interceptor;

    @BeforeEach
    void createRetryInterceptor() {
        interceptor = new TestRetryInterceptor(3);
    }

    @Test
    void shouldRetryWithConnectionException() {
        assertThat(interceptor.shouldRetry(1, "GET", new HttpTimeoutException("read timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(2, "GET", new ConnectException("connection failed"))).isTrue();
        assertThat(interceptor.shouldRetry(3, "GET", new ConnectException("connection failed"))).isFalse();

        assertThat(interceptor.shouldRetry(1, "POST", new HttpConnectTimeoutException("connection timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(1, "POST", new ConnectException("connection refused"))).isTrue();

        var timeout = new SocketTimeoutException("timeout");
        timeout.initCause(new SocketTimeoutException("Read timed out"));
        assertThat(interceptor.shouldRetry(1, "POST", timeout)).isFalse();
        assertThat(interceptor.shouldRetry(2, "PUT", timeout)).isTrue();
    }

    @Test
    void shouldRetryWithServiceUnavailable() {
        assertThat(interceptor.shouldRetry(1, HTTPStatus.OK.code)).isFalse();
        assertThat(interceptor.shouldRetry(1, HTTPStatus.SERVICE_UNAVAILABLE.code)).isTrue();
        assertThat(interceptor.shouldRetry(3, HTTPStatus.SERVICE_UNAVAILABLE.code)).isFalse();
    }

    @Test
    void closeResponseIfServiceUnavailable() throws IOException {
        var request = new Request.Builder().url("http://localhost").build();
        var source = mock(BufferedSource.class);
        var serviceUnavailableResponse = new Response.Builder().request(request)
                                                               .protocol(Protocol.HTTP_2)
                                                               .code(HTTPStatus.SERVICE_UNAVAILABLE.code)
                                                               .message("service unavailable")
                                                               .body(ResponseBody.create(source, MediaType.get("application/json"), 0))
                                                               .build();
        var okResponse = new Response.Builder().request(request)
                                               .protocol(Protocol.HTTP_2)
                                               .code(HTTPStatus.OK.code)
                                               .message("ok")
                                               .build();

        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(serviceUnavailableResponse).thenReturn(okResponse);

        interceptor.intercept(chain);

        verify(source).close();
    }

    static class TestRetryInterceptor extends RetryInterceptor {
        TestRetryInterceptor(int maxRetries) {
            super(maxRetries);
        }

        @Override
        void sleep(Duration waitTime) {
            // not sleep during test
        }
    }
}
