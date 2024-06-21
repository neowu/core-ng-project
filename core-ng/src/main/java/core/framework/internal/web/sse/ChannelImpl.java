package core.framework.internal.web.sse;

import core.framework.internal.log.filter.BytesLogParam;
import core.framework.log.ActionLogContext;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import core.framework.web.sse.Channel;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListener;
import org.xnio.channels.StreamSinkChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

class ChannelImpl<T> implements java.nio.channels.Channel, Channel<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelImpl.class);

    final String id = UUID.randomUUID().toString();
    final Set<String> groups = Sets.newConcurrentHashSet();
    final String refId;
    final long startTime = System.nanoTime();
    long lastSentTime = startTime;

    final WriteListener writeListener = new WriteListener();
    final Deque<byte[]> queue = new ConcurrentLinkedDeque<>();

    private final ChannelSupport<T> support;

    private final ReentrantLock lock = new ReentrantLock();
    private final HttpServerExchange exchange;
    private final StreamSinkChannel sink;

    private volatile boolean closed = false;

    public ChannelImpl(HttpServerExchange exchange, StreamSinkChannel sink, ChannelSupport<T> support, String refId) {
        this.exchange = exchange;
        this.sink = sink;
        this.support = support;
        this.refId = refId;
    }

    @Override
    public void send(String id, T event) {
        var watch = new StopWatch();
        byte[] data = support.data(event);
        byte[] message = support.message(id, data);
        try {
            send(message);
            lastSentTime = System.nanoTime();
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("sse", elapsed, 0, message.length);
            LOGGER.debug("send sse, channel={}, id={}, data={}, elapsed={}", this.id, id, new BytesLogParam(data), elapsed);
        }
    }

    void send(byte[] data) {
        if (closed) return;

        queue.add(data);
        exchange.getIoThread().execute(() -> writeListener.handleEvent(sink));
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() {
        try {
            lock.lock();
            if (closed) return;

            closed = true;
            queue.clear();
            exchange.endExchange();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void join(String group) {
        support.context.join(this, group);
    }

    @Override
    public void leave(String group) {
        support.context.leave(this, group);
    }

    ByteBuffer poll() {
        List<byte[]> bytes = new ArrayList<>(queue.size());

        while (true) {
            var data = queue.poll();
            if (data == null) break;
            bytes.add(data);
        }

        if (bytes.size() == 1)
            return ByteBuffer.wrap(bytes.getFirst());

        int length = 0;
        for (byte[] array : bytes) {
            length += array.length;
        }

        byte[] result = new byte[length];
        int index = 0;
        for (byte[] array : bytes) {
            System.arraycopy(array, 0, result, index, array.length);
            index += array.length;
        }

        return ByteBuffer.wrap(result);
    }

    private final class WriteListener implements ChannelListener<StreamSinkChannel> {
        private ByteBuffer buffer;

        @Override
        public void handleEvent(StreamSinkChannel channel) {
            try {
                lock.lock();

                if (buffer == null && queue.isEmpty()) {    // be called on browser refreshing, to notify old connection
                    if (channel.flush()) {                  // check if connection is broken, will trigger end exchange event
                        channel.suspendWrites();
                    }
                    return;
                }

                if (buffer == null) {
                    buffer = poll();
                }

                while (true) {
                    channel.write(buffer);
                    boolean flushed = channel.flush();

                    if (!flushed) {
                        channel.resumeWrites();
                        return;
                    }
                    if (!buffer.hasRemaining()) {
                        buffer = null;
                        channel.suspendWrites();
                        return;
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("failed to write sse message, error={}", e.getMessage(), e);
                exchange.endExchange();
            } finally {
                lock.unlock();
            }
        }
    }
}

