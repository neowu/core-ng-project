package core.framework.impl.redis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static core.framework.impl.redis.RedisEncodings.encode;

/**
 * @author neo
 */
class RedisConnection implements AutoCloseable {
    private static final int DEFAULT_PORT = 6379;

    RedisOutputStream outputStream;
    RedisInputStream inputStream;
    private Socket socket;

    void connect(String host, int timeoutInMs) throws IOException {
        socket = new Socket();
        socket.setReuseAddress(true);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true); // Socket buffer whether closed, to ensure timely delivery of data
        socket.setSoLinger(true, 0); // Control calls close () method, the underlying socket is closed immediately
        socket.connect(new InetSocketAddress(host, DEFAULT_PORT), timeoutInMs);
        socket.setSoTimeout(timeoutInMs);
        outputStream = new RedisOutputStream(socket.getOutputStream(), 8192);
        inputStream = new RedisInputStream(socket.getInputStream());
    }

    void writeKeyCommand(byte[] command, String key) throws IOException {
        writeArray(2);
        writeBulkString(command);
        writeBulkString(encode(key));
        flush();
    }

    void writeKeysCommand(byte[] command, String... keys) throws IOException {
        writeArray(1 + keys.length);
        writeBulkString(command);
        for (String key : keys) {
            writeBulkString(encode(key));
        }
        flush();
    }

    void writeKeyArgumentCommand(byte[] command, String key, byte[] argument) throws IOException {
        writeArray(3);
        writeBulkString(command);
        writeBulkString(encode(key));
        writeBulkString(argument);
        flush();
    }

    void writeKeyArgumentsCommand(byte[] command, String key, String... arguments) throws IOException {
        writeArray(2 + arguments.length);
        writeBulkString(command);
        writeBulkString(encode(key));
        for (String value : arguments) {
            writeBulkString(encode(value));
        }
        flush();
    }

    void writeArray(int length) throws IOException {
        Protocol.writeArray(outputStream, length);
    }

    void writeBulkString(byte[] value) throws IOException {
        Protocol.writeBulkString(outputStream, value);
    }

    void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        if (socket != null) socket.close();
    }

    String readSimpleString() throws IOException {
        return (String) Protocol.read(inputStream);
    }

    byte[] readBulkString() throws IOException {
        return (byte[]) Protocol.read(inputStream);
    }

    Long readLong() throws IOException {
        return (Long) Protocol.read(inputStream);
    }

    Object[] readArray() throws IOException {
        return (Object[]) Protocol.read(inputStream);
    }

    void readAll(int size) throws IOException {
        RedisException exception = null;
        for (int i = 0; i < size; i++) {
            try {
                Protocol.read(inputStream);
            } catch (RedisException e) {
                exception = e;
            }
        }
        if (exception != null) throw exception;
    }
}
