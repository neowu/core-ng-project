package core.framework.impl.redis.v2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author neo
 */
class RedisConnection implements AutoCloseable {
    private static final byte[][] EMPTY_ARGS = new byte[0][];

    private final Socket socket;
    private final OutputStream outputStream;
    private final RedisInputStream inputStream;

    RedisConnection(String host, int port, int timeout) throws IOException {
        socket = new Socket();
        socket.setReuseAddress(true);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true); // Socket buffer whether closed, to ensure timely delivery of data
        socket.setSoLinger(true, 0); // Control calls close () method, the underlying socket is closed immediately
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        outputStream = socket.getOutputStream();
        inputStream = new RedisInputStream(socket.getInputStream());
    }

    void sendRequest(Protocol.Command command) throws IOException {
        sendRequest(command, EMPTY_ARGS);
    }

    void sendRequest(Protocol.Command command, byte[]... arguments) throws IOException {
        try {
            byte[] request = Protocol.request(command, arguments);
            outputStream.write(request);
        } catch (IOException e) {
            try {
                String errorMessage = Protocol.readErrorIfPossible(inputStream);
                if (errorMessage != null && errorMessage.length() > 0) {
                    throw new IOException(errorMessage, e);
                }
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    String getSimpleStringResponse() throws IOException {
        Object response = Protocol.response(inputStream);
        if (response instanceof Protocol.Error) throw new RedisException(((Protocol.Error) response).message);
        return (String) response;
    }

    byte[] getBulkStringResponse() throws IOException {
        Object response = Protocol.response(inputStream);
        if (response instanceof Protocol.Error) throw new RedisException(((Protocol.Error) response).message);
        return (byte[]) response;
    }

    Long getLongResponse() throws IOException {
        Object response = Protocol.response(inputStream);
        if (response instanceof Protocol.Error) throw new RedisException(((Protocol.Error) response).message);
        return (Long) response;
    }

    Object[] getArrayResponse() throws IOException {
        Object response = Protocol.response(inputStream);
        if (response instanceof Protocol.Error) throw new RedisException(((Protocol.Error) response).message);
        return (Object[]) response;
    }
}
