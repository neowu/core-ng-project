package core.framework.impl.io;

import core.framework.api.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * for internal use, performance is top priority in design
 *
 * @author neo
 */
public final class ByteStreams {
    public static byte[] read(InputStream stream, int initialCapacity) {
        byte[] bytes = new byte[initialCapacity];
        int position = 0;
        try {
            while (true) {
                int bytesToRead = bytes.length - position;
                int bytesRead = stream.read(bytes, position, bytesToRead);
                if (bytesRead < 0) break;
                position += bytesRead;
                if (position >= bytes.length) {
                    byte[] newBytes = new byte[bytes.length * 2];
                    System.arraycopy(bytes, 0, newBytes, 0, position);
                    bytes = newBytes;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        byte[] result = new byte[position];
        System.arraycopy(bytes, 0, result, 0, position);
        return result;
    }

    public static byte[] readWithExpectedLength(InputStream stream, int expectedLength) {
        byte[] bytes = new byte[expectedLength];
        int position = 0;
        try {
            while (position < expectedLength) {
                int bytesRead = stream.read(bytes, position, expectedLength - position);
                if (bytesRead < 0) break;
                position += bytesRead;
            }
            if (stream.read() != -1) {
                throw new IOException(Strings.format("stream exceeds expected length, expected={}", expectedLength));
            }
            if (expectedLength != position)
                throw new IOException(Strings.format("stream ends prematurely, expected={}, actual={}", expectedLength, position));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes;
    }
}
