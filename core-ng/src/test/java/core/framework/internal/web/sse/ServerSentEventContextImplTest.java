package core.framework.internal.web.sse;

import core.framework.web.sse.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
        var channel = channel();
        channel.join("group1");
        List<Channel<TestEvent>> group = context.group("group1");
        assertThat(group).containsOnly(channel);

        channel.leave("group1");
        assertThat(context.group("group1")).isEmpty();
    }

    @Test
    void remove() {
        var channel1 = channel();
        channel1.join("group1");
        channel1.join("group2");
        var channel2 = channel();
        channel2.join("group1");
        channel2.join("group2");

        context.remove(channel1);
        assertThat(context.group("group1")).hasSize(1);
        assertThat(context.group("group2")).hasSize(1);

        context.remove(channel2);
        assertThat(context.group("group1")).isEmpty();
        assertThat(context.group("group2")).isEmpty();
    }

    @Test
    void all() {
        var channel = channel();
        context.add(channel);

        List<Channel<TestEvent>> all = context.all();
        assertThat(all).containsOnly(channel);

        context.remove(channel);
        assertThat(context.all()).isEmpty();
    }

    @Test
    void keepAlive() {
        ChannelImpl<TestEvent> channel = spy(channel());
        context.add(channel);
        context.keepAlive();

        channel.lastSentTime = 0;
        doNothing().when(channel).send(":\n");
        context.keepAlive();
        verify(channel, Mockito.times(1)).send(":\n");
    }

    private ChannelImpl<TestEvent> channel() {
        return new ChannelImpl<>(null, null, context, new ServerSentEventBuilder<>(TestEvent.class), null);
    }
}
