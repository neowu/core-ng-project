package core.framework.internal.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static core.framework.internal.redis.RedisEncodings.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisOutputStreamTest {
    private ByteArrayOutputStream output;
    private RedisOutputStream stream;

    @BeforeEach
    void createRedisOutputStream() {
        output = new ByteArrayOutputStream();
        stream = new RedisOutputStream(output, 4);
    }

    @Test
    void flush() throws IOException {
        stream.flush();
        assertEquals("", decode(output.toByteArray()));
    }

    @Test
    void writeBytesCRLF() throws IOException {
        stream.writeBytesCRLF(Strings.bytes("12345"));
        stream.flush();
        assertEquals("12345\r\n", decode(output.toByteArray()));
    }

    @Test
    void writeBytesCRLFWithinBuffer() throws IOException {
        stream.writeBytesCRLF(Strings.bytes("1234"));
        stream.flush();
        assertEquals("1234\r\n", decode(output.toByteArray()));
    }

    @Test
    void writeBytesCRLFEOverLeftBuffer() throws IOException {
        stream.write((byte) '1');
        stream.write((byte) '2');
        stream.writeBytesCRLF(Strings.bytes("3456"));
        stream.flush();
        assertEquals("123456\r\n", decode(output.toByteArray()));
    }

    @Test
    void write() throws IOException {
        stream.write((byte) '1');
        stream.write((byte) '2');
        stream.write((byte) '3');
        stream.write((byte) '4');
        stream.write((byte) '5');
        stream.flush();
        assertEquals("12345", decode(output.toByteArray()));
    }
}
