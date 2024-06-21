package core.framework.internal.web.sse;

import core.framework.web.sse.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ServerSentEventContextImplTest {
    private ServerSentEventContextImpl<TestEvent> context;

    @BeforeEach
    void createServerSentEventContextImpl() {
        context = new ServerSentEventContextImpl<>();
    }

    @Test
    void join() {
        final var channel = channel();
        channel.join("group1");
        List<Channel<TestEvent>> group = context.group("group1");
        assertThat(group).containsOnly(channel);

        channel.leave("group1");
        assertThat(context.group("group1")).isEmpty();
    }

    @Test
    void remove() {
        final var channel = channel();
        channel.join("group1");
        channel.join("group2");

        context.remove(channel);
        assertThat(context.group("group1")).isEmpty();
        assertThat(context.group("group2")).isEmpty();
    }

    @Test
    void all() {
        final var channel = channel();
        context.add(channel);

        List<Channel<TestEvent>> all = context.all();
        assertThat(all).containsOnly(channel);

        context.remove(channel);
        assertThat(context.all()).isEmpty();
    }

    private ChannelImpl<TestEvent> channel() {
        return new ChannelImpl<>(null, null, new ChannelSupport<>(null, TestEvent.class, context), null);
    }
}
