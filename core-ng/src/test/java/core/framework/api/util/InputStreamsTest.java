package core.framework.api.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author neo
 */
public class InputStreamsTest {
    @Test
    public void bytes() throws IOException {
        byte[] bytes = Strings.bytes("123456789012345678901234567890");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, InputStreams.bytes(stream, 10));
        }
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, InputStreams.bytes(stream, 1));
        }
    }

    @Test
    public void bytesWithEmptyStream() throws IOException {
        byte[] bytes = Strings.bytes("");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, InputStreams.bytes(stream, 10));
        }
    }

    @Test
    public void bytesWithExactBufferSize() throws IOException {
        byte[] bytes = Strings.bytes("1234567890");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, InputStreams.bytes(stream, 10));
        }
    }

    @Test
    public void readWithExpectedLength() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, InputStreams.bytesWithExpectedLength(stream, bytes.length));
        }
    }
}