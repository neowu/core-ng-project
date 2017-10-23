package core.framework.redis.impl;

import core.framework.util.Charsets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class RedisConnection implements AutoCloseable {
    private static final byte[][] EMPTY_ARGS = new byte[0][];

    private final Socket socket;
    private RedisOutputStream outputStream;
    private RedisInputStream inputStream;
    private int pipelinedCommands = 0;

    public RedisConnection(String host, int port, int timeout) throws IOException {
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

    public void sendCommand(final Protocol.Command cmd) throws IOException {
        sendCommand(cmd, EMPTY_ARGS);
    }

    public void sendCommand(final Protocol.Command cmd, final byte[]... args) throws IOException {
        try {
            Protocol.sendCommand(outputStream, cmd, args);
            pipelinedCommands++;
        } catch (IOException ex) {
            try {
                String errorMessage = Protocol.readErrorLineIfPossible(inputStream);
                if (errorMessage != null && errorMessage.length() > 0) {
                    throw new RedisException(errorMessage, ex.getCause());
                }
            } catch (Exception ignored) {
            }
            throw ex;
        }
    }

    @Override
    public void close() {
        try {
            outputStream.flush();
        } catch (IOException ex) {
            throw new RedisException(ex);
        } finally {
            closeQuietly(socket);
        }
    }

    private boolean connected() {
        return socket.isBound() && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    public String getStatusCodeReply() throws IOException {
        flush();
        pipelinedCommands--;
        final byte[] resp = (byte[]) Protocol.read(inputStream);
        if (null == resp) {
            return null;
        } else {
            return new String(resp, Charsets.UTF_8);
        }
    }

    public byte[] getBinaryResponse() throws IOException {
        flush();
        pipelinedCommands--;
        return (byte[]) Protocol.read(inputStream);
    }

    public Long getLongReply() throws IOException {
        flush();
        pipelinedCommands--;
        return (Long) Protocol.read(inputStream);
    }

    public List<byte[]> getMultiBinaryReply() throws IOException {
        flush();
        pipelinedCommands--;
        return (List<byte[]>) Protocol.read(inputStream);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getIntegerMultiBulkReply() throws IOException {
        flush();
        pipelinedCommands--;
        return (List<Long>) Protocol.read(inputStream);
    }

    public List<Object> getAll() throws IOException {
        return getAll(0);
    }

    public List<Object> getAll(int except) throws IOException {
        List<Object> all = new ArrayList<Object>();
        flush();
        while (pipelinedCommands > except) {
            try {
                all.add(Protocol.read(inputStream));
            } catch (RedisException e) {
                all.add(e);
            }
            pipelinedCommands--;
        }
        return all;
    }

    public Object getOne() throws IOException {
        flush();
        pipelinedCommands--;
        return Protocol.read(inputStream);
    }

    protected void flush() throws IOException {
        outputStream.flush();
    }

    private void closeQuietly(Socket sock) {
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }
}
