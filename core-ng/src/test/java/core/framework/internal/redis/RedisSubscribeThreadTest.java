package core.framework.internal.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class RedisSubscribeThreadTest {
    @Mock
    RedisChannelListener listener;
    private RedisSubscribeThread thread;

    @BeforeEach
    void createRedisSubscribeThread() {
        thread = new RedisSubscribeThread("name", null, listener, "channel");
    }

    @Test
    void close() throws IOException {
        thread.close();
    }

    @Test
    void process() throws Exception {
        var connection = new RedisConnection();
        connection.outputStream = new RedisOutputStream(new ByteArrayOutputStream(), 512);
        connection.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes("*3\r\n$9\r\nsubscribe\r\n$7\r\nchannel\r\n:1\r\n"
                + "*3\r\n$7\r\nmessage\r\n$7\r\nchannel\r\n$5\r\nvalue\r\n"
                + "+OK\r\n")));

        thread.process(connection);

        verify(listener).onSubscribe();
        verify(listener).onMessage(Strings.bytes("value"));
    }
}
