package core.framework.internal.web;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ShutdownHandlerTest {
    @Mock
    HttpServerExchange exchange;
    private ShutdownHandler handler;

    @BeforeEach
    void createShutdownHandler() {
        handler = new ShutdownHandler();
    }

    @Test
    void handle() {
        assertThat(handler.handle(exchange)).isFalse();
        handler.exchangeEvent(null, () -> {
        });
        assertThat(handler.activeRequests.get()).isEqualTo(0);
    }

    @Test
    void handleShutdown() {
        handler.shutdown();
        assertThat(handler.handle(exchange)).isTrue();

        verify(exchange).setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
        verify(exchange).setPersistent(false);
        verify(exchange).endExchange();
    }

    @Test
    void awaitTermination() throws InterruptedException {
        handler.activeRequests.increase();
        handler.awaitTermination(-1);
    }

    @Test
    void maxActiveRequests() {
        handler.activeRequests.increase();
        handler.activeRequests.increase();
        handler.handle(exchange);
        handler.activeRequests.decrease();
        assertThat(handler.activeRequests.max()).isEqualTo(3);
        assertThat(handler.activeRequests.max()).isEqualTo(2);

        handler.handle(exchange);
        assertThat(handler.activeRequests.max()).isEqualTo(3);
    }
}
