package core.framework.internal.web.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebSocketMessageListenerTest {
    private WebSocketMessageListener listener;

    @BeforeEach
    void createWebSocketMessageListener() {
        listener = new WebSocketMessageListener(null, null);
    }

    @Test
    void getMaxTextBufferSize() {
        assertThat(listener.getMaxTextBufferSize()).isGreaterThan(0);
    }
}
