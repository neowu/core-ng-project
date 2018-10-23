package core.framework.impl.web.response;

import core.framework.impl.web.ShutdownHandler;
import core.framework.web.CookieSpec;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ResponseHandlerTest {
    private ResponseHandler responseHandler;
    private ShutdownHandler shutdownHandler;

    @BeforeEach
    void createResponseHandler() {
        shutdownHandler = new ShutdownHandler();
        responseHandler = new ResponseHandler(null, null, shutdownHandler);
    }

    @Test
    void cookie() {
        CookieImpl cookie = responseHandler.cookie(new CookieSpec("test").secure(), "1=2");
        assertThat(cookie.getName()).isEqualTo("test");
        assertThat(cookie.getValue()).isEqualTo("1%3D2");
    }

    @Test
    void closeConnectionIfShutdown() {
        shutdownHandler.shutdown.set(true);
        var exchange = new HttpServerExchange(null);
        responseHandler.closeConnectionIfShutdown(exchange);
        assertThat(exchange.isPersistent()).isFalse();
    }
}
