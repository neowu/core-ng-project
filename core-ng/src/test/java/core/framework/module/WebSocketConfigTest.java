package core.framework.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.websocket.TestChannelListener;
import core.framework.internal.web.websocket.TestWebSocketMessage;
import core.framework.web.websocket.WebSocketContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketConfigTest {
    private WebSocketConfig config;

    @BeforeAll
    void createWebSocketConfig() {
        config = new WebSocketConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void withReservedPath() {
        assertThatThrownBy(() -> config.listen(HTTPIOHandler.HEALTH_CHECK_PATH, TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("/health-check is reserved path");
    }

    @Test
    void listen() {
        assertThatThrownBy(() -> config.listen("/ws/:name", null, null, null))
            .isInstanceOf(Error.class)
            .hasMessageContaining("listener path must be static");

        assertThatThrownBy(() -> config.listen("/ws", TestWebSocketMessage.class, TestWebSocketMessage.class, (channel, message) -> {
        })).isInstanceOf(Error.class)
            .hasMessageContaining("listener class must not be anonymous class or lambda");

        config.listen("/ws2", TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener());
        var webSocketContext = (WebSocketContext) config.context.beanFactory.bean(WebSocketContext.class, null);
        assertThat(webSocketContext).isNotNull();
        assertThat(config.context.apiController.beanClasses).contains(TestWebSocketMessage.class);
    }

    @Test
    void validate() {
        config.validate();
        assertThat(config.context.httpServer.handler.rateControl.hasGroup(WebSocketConfig.WS_OPEN_GROUP)).isTrue();
    }
}
