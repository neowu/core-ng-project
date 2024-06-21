package core.framework.internal.web.websocket;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ChannelImplTest {
    @Test
    void context() {
        WebSocketContextImpl<TestWebSocketMessage> context = new WebSocketContextImpl<>();
        var channel = new ChannelImpl<>(null, new ChannelSupport<>(TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener(), context));
        channel.context().put("k1", "v1");
        assertThat(channel.context().get("k1")).isEqualTo("v1");

        channel.context().put("k1", null);
        assertThat(channel.context().get("k1")).isNull();
    }
}
