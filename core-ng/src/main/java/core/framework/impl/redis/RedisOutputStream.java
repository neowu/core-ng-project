package core.framework.impl.redis;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author neo
 */
class RedisOutputStream {
    private final OutputStream outputStream;
    private final byte[] buffer = new byte[8192];
    private int position;

    RedisOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    void write(byte value) throws IOException {
        if (position == buffer.length) {
            flush();
        }
        buffer[position++] = value;
    }

    void writeBytesCRLF(byte[] bytes) throws IOException {
        int length = bytes.length;
        if (length >= buffer.length) {
            flush();
            outputStream.write(bytes);
        } else {
            if (length >= buffer.length - length) {
                flush();
            }
            System.arraycopy(bytes, 0, buffer, position, length);
            position += length;
        }

        if (2 >= buffer.length - position) {
            flush();
        }
        buffer[position++] = '\r';
        buffer[position++] = '\n';
    }

    void flush() throws IOException {
        if (position > 0) {
            outputStream.write(buffer, 0, position);
            position = 0;
        }
    }
}
