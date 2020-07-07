package core.framework.internal.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class RedisSubscribeThreadTest {
    private RedisSubscribeThread thread;
    private RedisConnection connection;
    private RedisChannelListener listener;

    @BeforeEach
    void createRedisSubscribeThread() {
        listener = mock(RedisChannelListener.class);

        RedisImpl redis = mock(RedisImpl.class);
        connection = new RedisConnection();
        connection.outputStream = new RedisOutputStream(new ByteArrayOutputStream(), 512);
        when(redis.createConnection(0)).thenReturn(connection);

        thread = new RedisSubscribeThread("name", redis, listener, "channel");
    }

    @Test
    void close() {
        thread.close();
    }

    @Test
    void process() throws IOException {
        connection.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes("*3\r\n$9\r\nsubscribe\r\n$7\r\nchannel\r\n:1\r\n" + "*3\r\n$7\r\nmessage\r\n$7\r\nchannel\r\n$5\r\nvalue\r\n" + "+OK\r\n")));

        thread.process();

        verify(listener).onSubscribe();
        verify(listener).onMessage(Strings.bytes("value"));
    }
}
