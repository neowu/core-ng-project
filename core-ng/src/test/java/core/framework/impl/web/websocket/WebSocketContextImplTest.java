package core.framework.impl.web.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WebSocketContextImplTest {
    private WebSocketContextImpl context;

    @BeforeEach
    void createWebSocketContextImpl() {
        context = new WebSocketContextImpl();
    }

    @Test
    void join() {
        var channel = new ChannelImpl(null, context, null);
        channel.join("room1");
        assertThat(context.room("room1")).containsOnly(channel);

        channel.leave("room1");
        assertThat(context.room("room1")).isEmpty();
    }

    @Test
    void remove() {
        var channel = new ChannelImpl(null, context, null);
        channel.join("room1");
        channel.join("room2");

        context.remove(channel);
        assertThat(context.room("room1")).isEmpty();
        assertThat(context.room("room2")).isEmpty();
    }
}
