package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.HTTPIOHandler;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;
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
    private ModuleContext context;

    @BeforeAll
    void createWebSocketConfig() {
        context = new ModuleContext();
        config = new WebSocketConfig(context);
    }

    @Test
    void withReservedPath() {
        assertThatThrownBy(() -> config.listen(HTTPIOHandler.HEALTH_CHECK_PATH, new TestChannelListener()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("/health-check is reserved path");
    }

    @Test
    void add() {
        config.listen("/ws2", new TestChannelListener());

        WebSocketContext webSocketContext = (WebSocketContext) context.beanFactory.bean(WebSocketContext.class, null);
        assertThat(webSocketContext).isNotNull();
    }

    static class TestChannelListener implements ChannelListener {
        @Override
        public void onMessage(Channel channel, String message) {

        }
    }
}
