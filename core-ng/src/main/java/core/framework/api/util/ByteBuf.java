package core.framework.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author neo
 */
public final class ByteBuf {
    // ByteBuf is not thread safe
    public static ByteBuf newBuffer() {
        return new ByteBuf(-1);
    }

    public static ByteBuf newBufferWithExpectedLength(int length) {
        if (length <= 0) throw Exceptions.error("expected length must greater than 0, length={}", length);
        return new ByteBuf(length);
    }

    byte[] bytes;
    private final int expectedLength;
    int position;

    private ByteBuf(int expectedLength) {
        if (expectedLength > 0) {
            bytes = new byte[expectedLength];
            this.expectedLength = expectedLength;
        } else {
            bytes = new byte[4096];    // 4k as minimal buffer
            this.expectedLength = -1;
        }
    }

    public void read(ByteBuffer buffer) throws IOException {
        int size = buffer.remaining();
        if (size == 0) return;

        if (position + size > bytes.length) {
            if (expectedLength > 0)
                throw new IOException("stream does not end as expected, expected=" + expectedLength);

            int newSize = bytes.length * 2;
            if (newSize < position + size) newSize = position + size;
            bytes = Arrays.copyOf(bytes, newSize);
        }

        buffer.get(bytes, position, size);
        position += size;
    }

    public void read(InputStream stream) throws IOException {
        if (expectedLength > 0) {
            readInputStreamWithExpectedLength(stream);
        } else {
            readInputStream(stream);
        }
    }

    private void readInputStreamWithExpectedLength(InputStream stream) throws IOException {
        while (position < expectedLength) {
            int bytesRead = stream.read(bytes, position, expectedLength - position);
            if (bytesRead < 0) break;
            position += bytesRead;
        }
        if (stream.read() != -1) {
            throw new IOException("stream does not end as expected, expected=" + expectedLength);
        }
    }

    private void readInputStream(InputStream stream) throws IOException {
        while (true) {
            int bytesToRead;
            if (position < bytes.length) {
                bytesToRead = bytes.length - position;
            } else {
                bytesToRead = bytes.length;    // double the buffer
                bytes = Arrays.copyOf(bytes, position * 2);
            }
            int bytesRead = stream.read(bytes, position, bytesToRead);
            if (bytesRead < 0) break;
            position += bytesRead;
        }
    }

    public int length() {
        return position;
    }

    public String text() {
        return text(Charsets.UTF_8);
    }

    public String text(Charset charset) {
        checkLength();
        return new String(bytes, 0, position, charset);
    }

    public byte[] bytes() {
        checkLength();
        if (position == bytes.length) return bytes;
        return Arrays.copyOf(bytes, position);
    }

    public ByteBuffer byteBuffer() {
        checkLength();
        return ByteBuffer.wrap(bytes, 0, position);
    }

    public InputStream inputStream() {
        checkLength();
        return new ByteBufInputStream(bytes, position);
    }

    private void checkLength() {
        if (expectedLength >= 0 && expectedLength != position)
            throw Exceptions.error("stream ends prematurely, expected={}, actual={}", expectedLength, position);
    }

    private static class ByteBufInputStream extends InputStream {
        final byte[] bytes;
        final int length;
        int position;

        ByteBufInputStream(byte[] bytes, int length) {
            this.bytes = bytes;
            this.length = length;
        }

        @Override
        public int read() throws IOException {
            if (position >= length) {
                return -1;
            }
            return bytes[position++];
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (position >= this.length) {
                return -1;
            }
            int availableLength = Math.min(length, this.length - position);
            System.arraycopy(this.bytes, position, bytes, offset, availableLength);
            position += availableLength;
            return availableLength;
        }
    }
}
