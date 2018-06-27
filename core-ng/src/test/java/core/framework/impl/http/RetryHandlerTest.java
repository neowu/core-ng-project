package core.framework.impl.http;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class RetryHandlerTest {
    private RetryHandler handler;
    private HttpClientContext clientContext;

    @BeforeEach
    void createRetryHandler() {
        clientContext = mock(HttpClientContext.class);
        handler = new RetryHandler(3);
    }

    @Test
    void waitTime() {
        assertThat(handler.waitTime(1)).isEqualTo(Duration.ofMillis(500));
        assertThat(handler.waitTime(2)).isEqualTo(Duration.ofMillis(1000));
        assertThat(handler.waitTime(3)).isEqualTo(Duration.ofMillis(2000));
        assertThat(handler.waitTime(4)).isEqualTo(Duration.ofMillis(4000));
    }

    @Test
    void retry() {
        HttpUriRequest request = RequestBuilder.get().build();
        request.abort();
        assertThat(handler.retry(request, clientContext, new IOException())).isFalse();
    }

    @Test
    void retryWithIdempotentMethods() {
        assertThat(handler.retry(RequestBuilder.get().build(), clientContext, new IOException())).isTrue();
        assertThat(handler.retry(RequestBuilder.put().build(), clientContext, new IOException())).isTrue();
    }

    @Test
    void retryWithNotSentRequest() {
        when(clientContext.isRequestSent()).thenReturn(false);

        assertThat(handler.retry(RequestBuilder.post().build(), clientContext, new IOException())).isTrue();
    }

    @Test
    void retryWithPersistentConnectionDrop() {
        assertThat(handler.retry(RequestBuilder.post().build(), clientContext, new NoHttpResponseException("connection failed"))).isTrue();
    }
}
