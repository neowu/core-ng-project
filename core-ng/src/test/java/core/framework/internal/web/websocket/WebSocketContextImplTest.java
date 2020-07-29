package core.framework.internal.web.websocket;

import core.framework.web.websocket.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        var channel = new ChannelImpl<TestWebSocketMessage, TestWebSocketMessage>(null, context, null);
        channel.join("room1");
        List<Channel<TestWebSocketMessage>> room = context.room("room1");
        assertThat(room).containsOnly(channel);

        channel.leave("room1");
        assertThat(context.room("room1")).isEmpty();
    }

    @Test
    void remove() {
        var channel = new ChannelImpl<TestWebSocketMessage, TestWebSocketMessage>(null, context, null);
        channel.join("room1");
        channel.join("room2");

        context.remove(channel);
        assertThat(context.room("room1")).isEmpty();
        assertThat(context.room("room2")).isEmpty();
    }

    @Test
    void all() {
        var channel = new ChannelImpl<TestWebSocketMessage, TestWebSocketMessage>(null, context, null);
        context.add(channel);

        List<Channel<TestWebSocketMessage>> all = context.all();
        assertThat(all).containsOnly(channel);

        context.remove(channel);
        assertThat(context.all()).isEmpty();
    }
}
