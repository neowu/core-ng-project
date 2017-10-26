package core.framework.impl.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static core.framework.impl.redis.RedisEncodings.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class ProtocolTest {
    @Test
    void write() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Protocol.write(new RedisOutputStream(stream), Protocol.Command.SET, RedisEncodings.encode("k1"), RedisEncodings.encode("v1"));
        assertEquals("*3\r\n$3\r\nSET\r\n$2\r\nk1\r\n$2\r\nv1\r\n", decode(stream.toByteArray()));
    }

    @Test
    void readError() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(Strings.bytes("-error-message\r\n"));
        RedisException exception = assertThrows(RedisException.class, () -> Protocol.read(new RedisInputStream(stream)));
        assertEquals("error-message", exception.getMessage());
    }

    @Test
    void readArray() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(Strings.bytes("*3\r\n$1\r\n1\r\n$1\r\n2\r\n$1\r\n3\r\n"));
        Object[] response = (Object[]) Protocol.read(new RedisInputStream(stream));
        assertEquals(3, response.length);
        assertEquals("1", decode((byte[]) response[0]));
        assertEquals("2", decode((byte[]) response[1]));
        assertEquals("3", decode((byte[]) response[2]));
    }
}
