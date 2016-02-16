package core.framework.impl.io;

import core.framework.api.util.Strings;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author neo
 */
public class ByteStreamsTest {
    @Test
    public void read() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, ByteStreams.read(stream, 10));
        }
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, ByteStreams.read(stream, 1));
        }
    }

    @Test
    public void readWithExpectedLength() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, ByteStreams.readWithExpectedLength(stream, bytes.length));
        }
    }
}