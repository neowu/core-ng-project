package core.framework.impl.web;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class ShutdownHandlerTest {
    private ShutdownHandler handler;
    private HttpServerExchange exchange;

    @BeforeEach
    void createShutdownHandler() {
        exchange = mock(HttpServerExchange.class);
        handler = new ShutdownHandler();
    }

    @Test
    void handle() {
        assertThat(handler.handle(exchange)).isFalse();
        handler.exchangeEvent(exchange, () -> {
        });
        assertThat(handler.activeRequests).hasValue(0);
    }

    @Test
    void handleShutdown() {
        handler.shutdown();
        assertThat(handler.handle(exchange)).isTrue();

        verify(exchange).setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
        verify(exchange).endExchange();
    }

    @Test
    void awaitTermination() throws InterruptedException {
        handler.activeRequests.getAndIncrement();
        handler.awaitTermination(-1);
    }
}
