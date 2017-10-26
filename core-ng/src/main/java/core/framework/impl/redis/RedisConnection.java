package core.framework.impl.redis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author neo
 */
class RedisConnection implements AutoCloseable {
    private final Socket socket;
    private final RedisOutputStream outputStream;
    private final RedisInputStream inputStream;

    RedisConnection(String host, int port, int timeout) throws IOException {
        socket = new Socket();
        socket.setReuseAddress(true);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true); // Socket buffer whether closed, to ensure timely delivery of data
        socket.setSoLinger(true, 0); // Control calls close () method, the underlying socket is closed immediately
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        outputStream = new RedisOutputStream(socket.getOutputStream());
        inputStream = new RedisInputStream(socket.getInputStream());
    }

    void write(Protocol.Command command, byte[]... arguments) throws IOException {
        Protocol.write(outputStream, command, arguments);
    }

    @Override
    public void close() throws IOException {
        socket.close();
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
