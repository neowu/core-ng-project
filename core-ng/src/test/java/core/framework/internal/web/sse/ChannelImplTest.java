package core.framework.internal.web.sse;

import core.framework.internal.web.request.RequestImpl;
import core.framework.util.Strings;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xnio.XnioIoThread;
import org.xnio.channels.StreamSinkChannel;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelImplTest {
    private ChannelImpl<TestEvent> channel;

    @BeforeEach
    void createChannelImpl() {
        StreamSinkChannel sink = mock(StreamSinkChannel.class);
        when(sink.getIoThread()).thenReturn(mock(XnioIoThread.class));
        ServerConnection connection = mock(ServerConnection.class);
        channel = new ChannelImpl<>(new HttpServerExchange(connection), sink, null, new ServerSentEventWriter<>(TestEvent.class), null, new RequestImpl(null, null));
    }

    @Test
    void poll() {
        channel.queue.add(Strings.bytes("1"));
        ByteBuffer buffer = channel.poll();
        assertThat(buffer.array()).isEqualTo(Strings.bytes("1"));

        channel.queue.add(Strings.bytes("1"));
        channel.queue.add(Strings.bytes("2"));
        channel.queue.add(Strings.bytes("3"));
        buffer = channel.poll();
        assertThat(buffer.array()).isEqualTo(Strings.bytes("123"));
    }

    @Test
    void send() {
        long oldTime = channel.lastSentTime;
        channel.send(new TestEvent());

        assertThat(channel.queue.size()).isEqualTo(1);
        assertThat(channel.lastSentTime).isGreaterThan(oldTime);
    }

    @Test
    void context() {
        channel.context().put("key", "value");
        assertThat(channel.context().get("key")).isEqualTo("value");

        channel.context().put("key", null);
        assertThat(channel.context().get("key")).isNull();
    }
}
