package core.framework.impl.redis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

/**
 * @author neo
 */
class RedisConnection implements AutoCloseable {
    private static final int DEFAULT_PORT = 6379;
    private final String host;
    private final int timeoutInMs;

    RedisOutputStream outputStream;
    RedisInputStream inputStream;

    private Socket socket;

    RedisConnection(String host, Duration timeout) {
        this.host = host;
        timeoutInMs = (int) timeout.toMillis();
    }

    void connect() throws IOException {
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

    void write(Protocol.Command command, byte[]... arguments) throws IOException {
        Protocol.write(outputStream, command, arguments);
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
