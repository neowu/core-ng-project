package core.framework.impl.web;

import core.framework.web.exception.ServiceUnavailableException;
import io.undertow.server.HttpServerExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ShutdownHandlerTest {
    private ShutdownHandler handler;
    private HttpServerExchange exchange;

    @BeforeEach
    void createShutdownHandler() {
        exchange = new HttpServerExchange(null, -1);
        handler = new ShutdownHandler();
    }

    @Test
    void handleRequest() {
        handler.handleRequest(exchange);
        handler.exchangeEvent(exchange, () -> {
        });

        assertThat(handler.activeRequests).hasValue(0);
    }

    @Test
    void shutdown() {
        handler.shutdown();
        assertThatThrownBy(() -> handler.handleRequest(exchange))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void awaitTermination() throws InterruptedException {
        handler.activeRequests.getAndIncrement();
        handler.awaitTermination(-1);
    }
}
