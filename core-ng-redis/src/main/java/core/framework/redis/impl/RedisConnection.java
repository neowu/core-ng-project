package core.framework.redis.impl;

import core.framework.util.Charsets;

import java.io.IOException;
import java.io.UncheckedIOException;
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

    public RedisConnection(String host, int port, int timeout) {
        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to ensure timely delivery of data
            socket.setSoLinger(true, 0); // Control calls close () method, the underlying socket is closed immediately
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);
            outputStream = new RedisOutputStream(socket.getOutputStream());
            inputStream = new RedisInputStream(socket.getInputStream());
        } catch (IOException ex) {
            broken = true;
            throw new UncheckedIOException(ex);
        }
    }

    public void sendCommand(final Protocol.Command cmd) {
        sendCommand(cmd, EMPTY_ARGS);
    }

    public void sendCommand(final Protocol.Command cmd, final byte[]... args) {
        try {
            Protocol.sendCommand(outputStream, cmd, args);
            pipelinedCommands++;
        } catch (IOException ex) {
            try {
                String errorMessage = Protocol.readErrorLineIfPossible(inputStream);
                if (errorMessage != null && errorMessage.length() > 0) {
                    throw new RedisException(errorMessage, ex.getCause());
                }
            } catch (Exception e) {
            }
            broken = true;
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() {
        if (connected()) {
            try {
                outputStream.flush();
            } catch (IOException ex) {
                broken = true;
                throw new RedisException(ex);
            } finally {
                closeQuietly(socket);
            }
        }
    }

    private boolean connected() {
        return socket.isBound() && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    public String getStatusCodeReply() {
        flush();
        pipelinedCommands--;
        final byte[] resp = (byte[]) readResponse();
        if (null == resp) {
            return null;
        } else {
            return new String(resp, Charsets.UTF_8);
        }
    }

    public String getBulkReply() {
        final byte[] result = getBinaryBulkReply();
        if (result != null) {
            return new String(result, Charsets.UTF_8);
        } else {
            return null;
        }
    }

    public byte[] getBinaryBulkReply() {
        flush();
        pipelinedCommands--;
        return (byte[]) readResponse();
    }

    public Long getIntegerReply() {
        flush();
        pipelinedCommands--;
        return (Long) readResponse();
    }

    public List<byte[]> getBinaryMultiBulkReply() {
        flush();
        pipelinedCommands--;
        return (List<byte[]>) readResponse();
    }

    @SuppressWarnings("unchecked")
    public List<Object> getRawObjectMultiBulkReply() {
        return (List<Object>) readResponse();
    }

    public List<Object> getObjectMultiBulkReply() {
        flush();
        pipelinedCommands--;
        return getRawObjectMultiBulkReply();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getIntegerMultiBulkReply() {
        flush();
        pipelinedCommands--;
        return (List<Long>) readResponse();
    }

    public List<Object> getAll() {
        return getAll(0);
    }

    public List<Object> getAll(int except) {
        List<Object> all = new ArrayList<Object>();
        flush();
        while (pipelinedCommands > except) {
            try {
                all.add(readResponse());
            } catch (RedisException e) {
                all.add(e);
            }
            pipelinedCommands--;
        }
        return all;
    }

    public Object getOne() {
        flush();
        pipelinedCommands--;
        return readResponse();
    }

    public boolean isBroken() {
        return broken;
    }

    protected void flush() {
        try {
            outputStream.flush();
        } catch (IOException ex) {
            broken = true;
            throw new RedisException(ex);
        }
    }

    protected Object readResponse() {
        try {
            return Protocol.read(inputStream);
        } catch (RedisException exc) {
            throw exc;
        } catch (IOException e) {
            broken = true;
            throw new UncheckedIOException(e);
        }
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
