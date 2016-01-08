package core.framework.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author neo
 */
public final class ByteBuf {
    // ByteBuf is not thread safe
    public static ByteBuf newBuffer(int initialCapacity) {
        return new ByteBuf(initialCapacity, -1);
    }

    public static ByteBuf newBufferWithExpectedLength(int length) {
        if (length < 0) throw Exceptions.error("expected length must not less than 0, length={}", length);
        return new ByteBuf(-1, length);
    }

    private final int expectedLength;
    byte[] bytes;
    int position;

    private ByteBuf(int initialCapacity, int expectedLength) {
        if (expectedLength >= 0) {
            bytes = new byte[expectedLength];
            this.expectedLength = expectedLength;
        } else {
            bytes = new byte[initialCapacity];
            this.expectedLength = -1;
        }
    }

    public void put(byte value) {
        ensureCapacity(1);
        bytes[position] = value;
        position++;
    }

    public void put(byte[] bytes, int offset, int length) {
        ensureCapacity(length);
        System.arraycopy(bytes, offset, this.bytes, position, length);
        position += length;
    }

    public void put(ByteBuffer buffer) {
        int size = buffer.remaining();
        if (size == 0) return;
        ensureCapacity(size);
        buffer.get(bytes, position, size);
        position += size;
    }

    public void put(InputStream stream) throws IOException {
        if (expectedLength >= 0) {
            readInputStreamWithExpectedLength(stream);
        } else {
            readInputStream(stream);
        }
    }

    private void ensureCapacity(int lengthToAdd) {
        int currentLength = bytes.length;
        int newLength = position + lengthToAdd;
        if (newLength > currentLength) {
            if (expectedLength > 0)
                throw Exceptions.error("input exceeds expected length, expected={}", expectedLength);

            int newSize = currentLength * 2;    // expand to 2x at least
            if (newSize < newLength) newSize = newLength;

            byte[] newBytes = new byte[newSize];
            System.arraycopy(bytes, 0, newBytes, 0, position);
            bytes = newBytes;
        }
    }

    private void readInputStreamWithExpectedLength(InputStream stream) throws IOException {
        while (position < expectedLength) {
            int bytesRead = stream.read(bytes, position, expectedLength - position);
            if (bytesRead < 0) break;
            position += bytesRead;
        }
        if (stream.read() != -1) {
            throw new IOException("input exceeds expected length, expected=" + expectedLength);
        }
    }

    private void readInputStream(InputStream stream) throws IOException {
        while (true) {
            ensureCapacity(1);
            int bytesToRead = bytes.length - position;
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
        byte[] result = new byte[position];
        System.arraycopy(bytes, 0, result, 0, position);
        return result;
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
        public int available() throws IOException {
            return Math.max(0, length - position);
        }

        @Override
        public long skip(long n) throws IOException {
            int actualSkipped = (int) Math.min(length - position, n);
            position += actualSkipped;
            return actualSkipped;
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
