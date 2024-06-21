package core.framework.internal.web.sse;

import core.framework.web.sse.ServerSentEventChannel;
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
        var channel = new ServerSentEventChannelImpl<>(null, null, context, null, null);
        channel.join("group1");
        List<ServerSentEventChannel<TestEvent>> group = context.group("group1");
        assertThat(group).containsOnly(channel);

        channel.leave("group1");
        assertThat(context.group("group1")).isEmpty();
    }

    @Test
    void remove() {
        var channel = new ServerSentEventChannelImpl<>(null, null, context, null, null);
        channel.join("group1");
        channel.join("group2");

        context.remove(channel);
        assertThat(context.group("group1")).isEmpty();
        assertThat(context.group("group2")).isEmpty();
    }

    @Test
    void all() {
        var channel = new ServerSentEventChannelImpl<>(null, null, context, null, null);
        context.add(channel);

        List<ServerSentEventChannel<TestEvent>> all = context.all();
        assertThat(all).containsOnly(channel);

        context.remove(channel);
        assertThat(context.all()).isEmpty();
    }
}
