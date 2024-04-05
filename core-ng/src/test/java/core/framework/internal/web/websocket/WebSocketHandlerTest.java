package core.framework.internal.web.websocket;

import core.framework.http.HTTPMethod;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.session.SessionImpl;
import core.framework.internal.web.session.SessionManager;
import core.framework.web.Session;
import core.framework.web.exception.BadRequestException;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        handler = new WebSocketHandler(new LogManager(), sessionManager, null);
    }

    @Test
    void add() {
        ChannelHandler<?, ?> handler = new ChannelHandler<>(TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener());
        this.handler.add("/ws", handler);
        assertThatThrownBy(() -> this.handler.add("/ws", handler))
                .isInstanceOf(Error.class)
                .hasMessageContaining("found duplicate channel listener");
    }

    @Test
    void checkWebSocket() {
        var headers = new HeaderMap()
                .put(Headers.SEC_WEB_SOCKET_KEY, "xxx")
            .put(Headers.SEC_WEB_SOCKET_VERSION, "13")
            .put(Headers.UPGRADE, "websocket");

        assertThat(handler.checkWebSocket(HTTPMethod.GET, headers)).isTrue();
        assertThat(handler.checkWebSocket(HTTPMethod.PUT, headers)).isFalse();

        assertThatThrownBy(() -> handler.checkWebSocket(HTTPMethod.GET, headers.put(Headers.SEC_WEB_SOCKET_VERSION, "07")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("only support web socket version 13");

        headers.remove(Headers.UPGRADE);
        assertThatThrownBy(() -> handler.checkWebSocket(HTTPMethod.GET, headers))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("upgrade is not permitted");
    }

    @Test
    void loadSession() {
        when(sessionManager.load(any(), any())).thenReturn(new SessionImpl("localhost"));
        Session session = handler.loadSession(null, null);
        assertThat(session).isInstanceOf(ReadOnlySession.class);

        when(sessionManager.load(any(), any())).thenReturn(null);
        session = handler.loadSession(null, null);
        assertThat(session).isNull();
    }
}
