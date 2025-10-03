package core.framework.internal.web.sse;

import core.framework.internal.log.filter.BytesLogParam;
import core.framework.log.ActionLogContext;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import core.framework.web.sse.Channel;
import io.undertow.server.HttpServerExchange;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListener;
import org.xnio.channels.StreamSinkChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

class ChannelImpl<T> implements java.nio.channels.Channel, Channel<T>, Channel.Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelImpl.class);

    final String id = UUID.randomUUID().toString();
    final Set<String> groups = Sets.newConcurrentHashSet();
    final String refId;
    final long startTime = System.nanoTime();

    final WriteListener writeListener = new WriteListener();
    final Deque<byte[]> queue = new ConcurrentLinkedDeque<>();

    private final ServerSentEventContextImpl<T> serverSentEventContext;
    private final ServerSentEventWriter<T> builder;

    private final ReentrantLock lock = new ReentrantLock();
    private final HttpServerExchange exchange;
    private final StreamSinkChannel sink;
    private final Map<String, Object> context = new ConcurrentHashMap<>();
    long lastSentTime = startTime;
    long eventCount;
    long eventSize;
    String clientIP;
    @Nullable
    String traceId;
    private volatile boolean closed = false;

    ChannelImpl(HttpServerExchange exchange, StreamSinkChannel sink, ServerSentEventContextImpl<T> serverSentEventContext, ServerSentEventWriter<T> builder, String refId) {
        this.exchange = exchange;
        this.sink = sink;
        this.serverSentEventContext = serverSentEventContext;
        this.builder = builder;
        this.refId = refId;
    }

    @Override
    public boolean send(@Nullable String id, T event) {
        String message = builder.toMessage(id, event);
        return sendBytes(Strings.bytes(message));
    }

    @Override
    public Context context() {
        return this;
    }

    boolean sendBytes(byte[] event) {
        if (closed) return false;

        var watch = new StopWatch();
        try {
            queue.add(event);
            sink.getIoThread().execute(() -> writeListener.handleEvent(sink));

            lastSentTime = System.nanoTime();
            eventCount++;
            eventSize += event.length;

            return true;
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("sse", elapsed, 0, event.length);
            LOGGER.debug("send sse message, channel={}, message={}, elapsed={}", id, new BytesLogParam(event), elapsed); // message is not in json format, not masked, assume sse won't send any sensitive event
        }
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() {
        LOGGER.debug("close sse connection, channel={}", id);
        sink.getIoThread().execute(() -> {
            try {
                lock.lock();
                if (closed) return;

                // for flow like
                // channel.sendBytes("error");
                // channel.close();
                // the close logic (this closure) could run first in different IO thread while sendBytes is blocked due to lock
                // so here we should try to send remaining messages in queue before close
                if (!queue.isEmpty()) {
                    writeListener.handleEvent(sink);
                }
                exchange.endExchange();
                closed = true;  // make sure mark closed after endExchange, to make sure no leak of exchange
            } finally {
                lock.unlock();
            }
        });
    }

    public void shutdown() {
        try {
            lock.lock();
            if (closed) return;

            LOGGER.debug("shutdown sse connection, channel={}", id);
            queue.clear();
            exchange.endExchange();
            closed = true;  // make sure mark closed after endExchange, to make sure no leak of exchange
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void join(String group) {
        serverSentEventContext.join(this, group);
    }

    @Override
    public void leave(String group) {
        serverSentEventContext.leave(this, group);
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

    @Nullable
    @Override
    public Object get(String key) {
        return context.get(key);
    }

    @Override
    public void put(String key, @Nullable Object value) {
        if (value == null) context.remove(key);
        else context.put(key, value);
    }

    private final class WriteListener implements ChannelListener<StreamSinkChannel> {
        @Nullable
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

