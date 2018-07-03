package core.framework.impl.redis;

import java.io.IOException;
import java.io.OutputStream;

/**
 * refer to https://github.com/xetorthio/jedis/blob/master/src/main/java/redis/clients/util/RedisOutputStream.java
 * @author neo
 */
class RedisOutputStream {
    private final OutputStream outputStream;
    private final byte[] buffer;
    private int position;

    RedisOutputStream(OutputStream outputStream, int bufferSize) {
        this.outputStream = outputStream;
        buffer = new byte[bufferSize];
    }

    void write(byte value) throws IOException {
        if (position == buffer.length) {
            flush();
        }
        buffer[position++] = value;
    }

    void writeBytesCRLF(byte[] bytes) throws IOException {
        int length = bytes.length;
        if (length > buffer.length) {
            flush();
            outputStream.write(bytes);
        } else {
            if (length > buffer.length - position) {
                flush();
            }
            System.arraycopy(bytes, 0, buffer, position, length);
            position += length;
        }

        if (buffer.length - position <= 2) {
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
