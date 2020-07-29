package core.framework.internal.web.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ChannelCloseListenerTest {
    private ChannelCloseListener listener;
    private WebSocketContextImpl context;

    @BeforeEach
    void createChannelCloseListener() {
        context = new WebSocketContextImpl();
        listener = new ChannelCloseListener(context);
    }

    @Test
    void remove() {
        var channel = new ChannelImpl<TestWebSocketMessage, TestWebSocketMessage>(null, context, null);
        context.join(channel, "room1");
        assertThat(context.room("room1")).hasSize(1);

        listener.remove(channel);
        assertThat(context.room("room1")).isEmpty();
    }
}
