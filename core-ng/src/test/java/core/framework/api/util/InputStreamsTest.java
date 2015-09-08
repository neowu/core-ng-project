package core.framework.api.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author neo
 */
public class InputStreamsTest {
    @Test
    public void readAllWithExpectedSize() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            byte[] readBytes = InputStreams.readAllWithExpectedSize(stream, bytes.length);
            Assert.assertArrayEquals(bytes, readBytes);
        }
    }

    @Test
    public void readAll() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            byte[] readBytes = InputStreams.readAll(stream);
            Assert.assertArrayEquals(bytes, readBytes);
        }
    }
}