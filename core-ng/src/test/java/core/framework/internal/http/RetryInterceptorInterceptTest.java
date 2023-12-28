package core.framework.internal.http;

import core.framework.api.http.HTTPStatus;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class RetryInterceptorInterceptTest {
    @Mock
    Interceptor.Chain chain;
    @Mock
    Call call;
    private Request request;
    private RetryInterceptor interceptor;

    @BeforeEach
    void createRetryInterceptor() {
        interceptor = new TestRetryInterceptor();

        request = new Request.Builder().url("http://localhost").build();
        when(chain.request()).thenReturn(request);
    }

    @Test
    void closeResponseIfServiceUnavailable() throws IOException {
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
        when(chain.proceed(request)).thenReturn(serviceUnavailableResponse).thenReturn(okResponse);

        Response response = interceptor.intercept(chain);
        assertThat(response).isSameAs(okResponse);

        verify(source).close();
    }

    @Test
    void retryWithConnectionFailure() throws IOException {
        var okResponse = new Response.Builder().request(request)
            .protocol(Protocol.HTTP_2)
            .code(HTTPStatus.OK.code)
            .message("ok")
            .build();
        when(chain.proceed(request)).thenThrow(new ConnectException("connection refused")).thenReturn(okResponse);
        when(chain.call()).thenReturn(call);

        Response response = interceptor.intercept(chain);
        assertThat(response).isSameAs(okResponse);
    }

    @Test
    void failedToRetry() throws IOException {
        when(chain.proceed(request)).thenThrow(new ConnectException("connection refused"));
        when(chain.call()).thenReturn(call);

        assertThatThrownBy(() -> interceptor.intercept(chain))
            .isInstanceOf(ConnectException.class)
            .hasMessageContaining("connection refused");

        verify(chain, times(3)).proceed(request);
    }

    @Test
    void callTimeout() throws IOException {
        var timeoutException = new IOException("Canceled");
        when(chain.proceed(request)).thenThrow(timeoutException);    // refer to RetryAndFollowUpInterceptor Line 72
        when(chain.call()).thenReturn(call);
        when(call.isCanceled()).thenReturn(Boolean.TRUE);

        assertThatThrownBy(() -> interceptor.intercept(chain))
            .isSameAs(timeoutException);

        verify(chain, times(1)).proceed(request);
    }

    static class TestRetryInterceptor extends RetryInterceptor {
        TestRetryInterceptor() {
            super(3, Duration.ofMillis(500));
        }

        @Override
        void sleep(Duration duration) {
            // skip sleep
        }
    }
}
