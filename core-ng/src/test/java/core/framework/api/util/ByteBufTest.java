package core.framework.api.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

/**
 * @author neo
 */
public class ByteBufTest {
    @Test
    public void readByteBufferWithExpectedLength() throws IOException {
        String text = "12345678901234567890";
        byte[] bytes = Strings.bytes(text);
        ByteBuf buffer = ByteBuf.newBufferWithExpectedLength(bytes.length);
        buffer.read(ByteBuffer.wrap(bytes));

        Assert.assertEquals(text, buffer.text());
        Assert.assertArrayEquals(bytes, buffer.bytes);
    }

    @Test
    public void readByteBufferWithoutExpectedLength() throws IOException {
        String text = "12345678901234567890";
        byte[] bytes = Strings.bytes(text);
        ByteBuf buffer = ByteBuf.newBuffer();
        buffer.read(ByteBuffer.wrap(bytes, 0, 10));
        buffer.read(ByteBuffer.wrap(bytes, 10, bytes.length - 10));

        Assert.assertEquals(text, buffer.text());
        Assert.assertEquals(bytes.length, buffer.position);
    }

    @Test
    public void readInputStreamWithExpectedLength() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            ByteBuf buffer = ByteBuf.newBufferWithExpectedLength(bytes.length);
            buffer.read(stream);
            Assert.assertArrayEquals(bytes, buffer.bytes);
        }
    }

    @Test
    public void readInputStreamWithoutExpectedLength() throws IOException {
        byte[] bytes = Strings.bytes("12345678");
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            ByteBuf buffer = ByteBuf.newBufferWithExpectedLength(bytes.length);
            buffer.read(stream);
            Assert.assertArrayEquals(bytes, buffer.bytes());
        }
    }

    @Test
    public void growCapacity() throws IOException {
        String text = "12345678901234567890";
        byte[] bytes = Strings.bytes(text);
        ByteBuf buffer = ByteBuf.newBuffer();

        buffer.bytes = new byte[4];
        buffer.read(ByteBuffer.wrap(bytes, 0, 6));
        Assert.assertEquals("grow 2x size", 8, buffer.bytes.length);
        Assert.assertEquals(6, buffer.position);

        buffer.read(ByteBuffer.wrap(bytes, 6, 14));
        Assert.assertEquals("grow to available size", 20, buffer.bytes.length);
        Assert.assertEquals(20, buffer.position);

        Assert.assertEquals(text, buffer.text());
    }

    @Test
    public void inputStream() throws IOException {
        byte[] bytes = Strings.bytes("1234567890\n1234567890");
        ByteBuf buffer = ByteBuf.newBuffer();
        buffer.read(ByteBuffer.wrap(bytes));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(buffer.inputStream()))) {
            String line1 = reader.readLine();
            Assert.assertEquals("1234567890", line1);
            String line2 = reader.readLine();
            Assert.assertEquals("1234567890", line2);
            String line3 = reader.readLine();
            Assert.assertNull(line3);
        }
    }

    @Test
    public void bytes() throws IOException {
        byte[] bytes = Strings.bytes("12345678901234567890");
        ByteBuf buffer = ByteBuf.newBuffer();
        buffer.read(ByteBuffer.wrap(bytes));
        Assert.assertArrayEquals(bytes, buffer.bytes());
    }

    @Test
    public void emptyByteBuff() throws IOException {
        ByteBuf buffer = ByteBuf.newBufferWithExpectedLength(0);
        Assert.assertEquals("", buffer.text());

        byte[] bytes = new byte[0];
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            buffer.read(stream);
        }

        Assert.assertArrayEquals(bytes, buffer.bytes);
    }
}