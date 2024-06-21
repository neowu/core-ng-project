package core.framework.internal.web.websocket;

import core.framework.internal.log.LogManager;
import core.framework.internal.web.HTTPHandlerContext;
import core.framework.internal.web.session.SessionManager;
import core.framework.web.exception.BadRequestException;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class WebSocketHandlerTest {
    @Mock
    SessionManager sessionManager;
    private WebSocketHandler handler;

    @BeforeEach
    void createWebSocketHandler() {
        handler = new WebSocketHandler(new LogManager(), sessionManager, new HTTPHandlerContext());
    }

    @Test
    void add() {
        ChannelSupport<?, ?> handler = new ChannelSupport<>(TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener(), new WebSocketContextImpl<>());
        this.handler.add("/ws", handler);
        assertThatThrownBy(() -> this.handler.add("/ws", handler))
            .isInstanceOf(Error.class)
            .hasMessageContaining("found duplicate channel listener");
    }

    @Test
    void check() {
        var headers = new HeaderMap()
            .put(Headers.SEC_WEB_SOCKET_KEY, "xxx")
            .put(Headers.SEC_WEB_SOCKET_VERSION, "13")
            .put(Headers.UPGRADE, "websocket");
        assertThat(handler.check(Methods.GET, headers)).isTrue();
        assertThat(handler.check(Methods.PUT, headers)).isFalse();
    }

    @Test
    void validateWSHeaders() {
        var headers = new HeaderMap()
            .put(Headers.SEC_WEB_SOCKET_KEY, "xxx")
            .put(Headers.SEC_WEB_SOCKET_VERSION, "13")
            .put(Headers.UPGRADE, "websocket");

        assertThatThrownBy(() -> handler.validateWebSocketHeaders(headers.put(Headers.SEC_WEB_SOCKET_VERSION, "07")))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("only support web socket version 13");

        headers.remove(Headers.UPGRADE);
        assertThatThrownBy(() -> handler.validateWebSocketHeaders(headers))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("upgrade is not permitted");
    }
}
