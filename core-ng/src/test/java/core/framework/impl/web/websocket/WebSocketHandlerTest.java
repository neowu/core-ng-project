package core.framework.impl.web.websocket;

import core.framework.http.HTTPMethod;
import core.framework.impl.log.LogManager;
import core.framework.web.exception.BadRequestException;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class WebSocketHandlerTest {
    private WebSocketHandler handler;

    @BeforeEach
    void createWebSocketHandler() {
        handler = new WebSocketHandler(new LogManager());
    }

    @Test
    void add() {
        assertThatThrownBy(() -> handler.add("/ws/:name", (channel, message) -> {
        })).isInstanceOf(Error.class)
           .hasMessageContaining("web socket path must be static");

        assertThatThrownBy(() -> handler.add("/ws", (channel, message) -> {
        })).isInstanceOf(Error.class)
           .hasMessageContaining("listener class must not be anonymous class or lambda");
    }

    @Test
    void checkWebSocket() {
        var headers = new HeaderMap()
                .put(Headers.SEC_WEB_SOCKET_KEY, "xxx")
                .put(Headers.SEC_WEB_SOCKET_VERSION, "13");

        assertThat(handler.checkWebSocket(HTTPMethod.GET, headers)).isTrue();
        assertThat(handler.checkWebSocket(HTTPMethod.PUT, headers)).isFalse();

        assertThatThrownBy(() -> handler.checkWebSocket(HTTPMethod.GET, headers.put(Headers.SEC_WEB_SOCKET_VERSION, "07")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("only support web socket version 13");
    }
}
