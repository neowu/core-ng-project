package core.framework.internal.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ByteArrayBodyHandlerTest {
    private ByteArrayBodyHandler handler;
    private HttpResponse.ResponseInfo response;

    @BeforeEach
    void createByteArrayBodyHandler() {
        handler = new ByteArrayBodyHandler();
        response = mock(HttpResponse.ResponseInfo.class);
    }

    @Test
    void bodyWithNoContentStatus() {
        when(response.statusCode()).thenReturn(204);
        CompletionStage<byte[]> body = handler.apply(response).getBody();
        assertThat(body.toCompletableFuture()).isCompletedWithValueMatching(value -> Arrays.equals(new byte[0], value));
    }

    @Test
    void bodyWithOKSta() {
        when(response.statusCode()).thenReturn(200);
        CompletionStage<byte[]> body = handler.apply(response).getBody();
        assertThat(body.toCompletableFuture()).isNotCompleted();
    }
}
