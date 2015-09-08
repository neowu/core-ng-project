package core.framework.api.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author neo
 */
public final class InputStreams {
    // by following rule: who created InputStream close it, the place to close InputStream needs to handle IOException anyway
    public static byte[] readAllWithExpectedSize(InputStream stream, int size) throws IOException {
        byte[] bytes = new byte[size];
        int offset = 0;
        while (offset < size) {
            int bytesRead = stream.read(bytes, offset, size - offset);
            if (bytesRead < 0) break;
            offset += bytesRead;
        }
        if (offset < size) {
            throw new EOFException("stream ends prematurely, expected=" + size + ", actual=" + offset);
        } else if (stream.read() != -1) {
            throw new IOException("stream does not end as expected, expected=" + size);
        }
        return bytes;
    }

    // java.io.ByteArrayOutputStream is slow impl due to synchronization, refer to sun.misc.IOUtils.readFully
    public static byte[] readAll(InputStream stream) throws IOException {
        byte[] bytes = new byte[16384];     // use 16k as default buffer
        int position = 0;
        while (true) {
            int bytesToRead;
            if (position >= bytes.length) {
                bytesToRead = bytes.length;    // double the buffer
                bytes = Arrays.copyOf(bytes, position + bytesToRead);
            } else {
                bytesToRead = bytes.length - position;
            }
            int bytesRead = stream.read(bytes, position, bytesToRead);
            if (bytesRead < 0) {
                bytes = Arrays.copyOf(bytes, position);
                break;
            }
            position += bytesRead;
        }
        return bytes;
    }
}
