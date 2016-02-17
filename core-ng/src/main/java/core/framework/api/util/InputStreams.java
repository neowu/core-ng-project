package core.framework.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * @author neo
 */
public final class InputStreams {
    // this method does not close stream, it's responsibility of creator of stream
    public static byte[] bytes(InputStream stream, int initialBufferSize) {
        List<byte[]> buffers = Lists.newArrayList();
        int total = 0;
        byte[] currentBuffer = new byte[initialBufferSize];
        buffers.add(currentBuffer);
        int currentBufferPosition = 0;
        try {
            while (true) {
                int bytesToRead = currentBuffer.length - currentBufferPosition;
                int bytesRead = stream.read(currentBuffer, currentBufferPosition, bytesToRead);
                if (bytesRead < 0) break;
                currentBufferPosition += bytesRead;
                total += bytesRead;
                if (currentBufferPosition >= currentBuffer.length) {
                    currentBuffer = new byte[currentBuffer.length << 1];
                    buffers.add(currentBuffer);
                    currentBufferPosition = 0;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        byte[] result = new byte[total];
        int position = 0;
        for (byte[] buffer : buffers) {
            int length = Math.min(buffer.length, total - position);
            System.arraycopy(buffer, 0, result, position, length);
            position += length;
            if (position >= total) break;
        }
        return result;
    }

    public static byte[] bytesWithExpectedLength(InputStream stream, int expectedLength) {
        byte[] bytes = new byte[expectedLength];
        int position = 0;
        try {
            while (position < expectedLength) {
                int bytesRead = stream.read(bytes, position, expectedLength - position);
                if (bytesRead < 0) break;
                position += bytesRead;
            }
            if (stream.read() != -1)
                throw new IOException(Strings.format("stream exceeds expected length, expected={}", expectedLength));
            if (expectedLength != position)
                throw new IOException(Strings.format("stream ends prematurely, expected={}, actual={}", expectedLength, position));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes;
    }
}
