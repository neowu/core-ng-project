package core.framework.internal.web.sse;

import core.framework.util.Strings;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xnio.XnioIoThread;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerSentEventChannelImplTest {
    private ServerSentEventChannelImpl<TestEvent> channel;

    @BeforeEach
    void createServerSentEventChannelImpl() {
        ServerConnection connection = mock(ServerConnection.class);
        when(connection.getIoThread()).thenReturn(mock(XnioIoThread.class));
        channel = new ServerSentEventChannelImpl<>(new HttpServerExchange(connection), null, null, new ServerSentEventBuilder<>(TestEvent.class), null);
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
        channel.send(new TestEvent());

        assertThat(channel.queue.size()).isEqualTo(1);
    }
}
