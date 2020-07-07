package core.framework.internal.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class RedisSubscribeThreadTest {
    private RedisSubscribeThread thread;
    private RedisChannelListener listener;

    @BeforeEach
    void createRedisSubscribeThread() {
        listener = mock(RedisChannelListener.class);

        thread = new RedisSubscribeThread("name", null, listener, "channel");
    }

    @Test
    void close() throws IOException {
        thread.close();
    }

    @Test
    void process() throws IOException {
        var connection = new RedisConnection();
        connection.outputStream = new RedisOutputStream(new ByteArrayOutputStream(), 512);
        connection.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes("*3\r\n$9\r\nsubscribe\r\n$7\r\nchannel\r\n:1\r\n" + "*3\r\n$7\r\nmessage\r\n$7\r\nchannel\r\n$5\r\nvalue\r\n" + "+OK\r\n")));

        thread.process(connection);

        verify(listener).onSubscribe();
        verify(listener).onMessage(Strings.bytes("value"));
    }
}
