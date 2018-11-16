package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RetryInterceptorTest {
    private RetryInterceptor interceptor;

    @BeforeEach
    void createRetryInterceptor() {
        interceptor = new RetryInterceptor(3);
    }

    @Test
    void shouldRetryWithConnectionException() {
        assertThat(interceptor.shouldRetry(1, "GET", new HttpTimeoutException("read timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(2, "GET", new ConnectException("connection failed"))).isTrue();
        assertThat(interceptor.shouldRetry(3, "GET", new ConnectException("connection failed"))).isFalse();

        assertThat(interceptor.shouldRetry(1, "POST", new HttpConnectTimeoutException("connection timeout"))).isTrue();
        assertThat(interceptor.shouldRetry(1, "POST", new ConnectException("connection refused"))).isTrue();

        SocketTimeoutException timeout = new SocketTimeoutException("timeout");
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
}
