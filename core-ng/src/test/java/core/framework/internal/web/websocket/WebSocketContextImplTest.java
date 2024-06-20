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
    private WebSocketContextImpl<TestWebSocketMessage> context;

    @BeforeEach
    void createWebSocketContextImpl() {
        context = new WebSocketContextImpl<>();
    }

    @Test
    void join() {
        var channel = channel();
        channel.join("group1");
        List<Channel<TestWebSocketMessage>> group = context.group("group1");
        assertThat(group).containsOnly(channel);

        channel.leave("group1");
        assertThat(context.group("group1")).isEmpty();
    }

    @Test
    void remove() {
        var channel = channel();
        channel.join("group1");
        channel.join("group2");

        context.remove(channel);
        assertThat(context.group("group1")).isEmpty();
        assertThat(context.group("group2")).isEmpty();
    }

    @Test
    void all() {
        var channel = channel();
        context.add(channel);

        List<Channel<TestWebSocketMessage>> all = context.all();
        assertThat(all).containsOnly(channel);

        context.remove(channel);
        assertThat(context.all()).isEmpty();
    }

    private ChannelImpl<TestWebSocketMessage, TestWebSocketMessage> channel() {
        return new ChannelImpl<>(null, new ChannelHandler<>(TestWebSocketMessage.class, TestWebSocketMessage.class, new TestChannelListener(), context));
    }
}
