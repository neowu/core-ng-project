package core.framework.internal.web.websocket;

import core.framework.internal.log.Trace;
import core.framework.log.ActionLogContext;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import core.framework.web.websocket.Channel;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author neo
 */
public class ChannelImpl<T, V> implements Channel<V>, Channel.Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelImpl.class);
    final String id = UUID.randomUUID().toString();
    final long startTime = System.nanoTime();
    final Set<String> rooms = Sets.newConcurrentHashSet();
    final ChannelHandler<T, V> handler;
    private final WebSocketChannel channel;
    private final Map<String, Object> context = new ConcurrentHashMap<>();
    private final WebSocketContextImpl webSocketContext;
    String action;
    String clientIP;
    String refId;
    Trace trace;
    @Nullable
    CloseMessage closeMessage;

    ChannelImpl(WebSocketChannel channel, WebSocketContextImpl webSocketContext, ChannelHandler<T, V> handler) {
        this.channel = channel;
        this.webSocketContext = webSocketContext;
        this.handler = handler;
    }

    @Override
    public void send(V message) {
        var watch = new StopWatch();
        String text = handler.toServerMessage(message);

        // refer to io.undertow.websockets.core.WebSocketChannel.send(WebSocketFrameType),
        // in concurrent env, one thread can still get hold of channel from context right before channel close listener removes it from context
        // this is to reduce chance of triggering WebSocketMessages.MESSAGES.channelClosed() exception
        // but in theory, there is still small possibility to cause channelClosed()
        if (channel.isCloseFrameSent() || channel.isCloseFrameReceived()) return;

        try {
            WebSockets.sendText(text, channel, ChannelCallback.INSTANCE);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("ws", elapsed, 0, text.length());
            LOGGER.debug("send ws message, id={}, text={}, elapsed={}", id, text, elapsed);     // not mask, assume ws message not containing sensitive info, the text can be json or plain text
        }
    }

    @Override
    public void close() {
        var watch = new StopWatch();
        try {
            WebSockets.sendClose(WebSocketCloseCodes.NORMAL_CLOSURE, null, channel, ChannelCallback.INSTANCE);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("ws", elapsed, 0, 1);    // close message size = 1
            LOGGER.debug("close ws channel, id={}, elapsed={}", id, elapsed);
        }
    }

    @Override
    public void join(String room) {
        webSocketContext.join(this, room);
    }

    @Override
    public void leave(String room) {
        webSocketContext.leave(this, room);
    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public Object get(String key) {
        return context.get(key);
    }

    @Override
    public void put(String key, Object value) {
        if (value == null) context.remove(key);
        else context.put(key, value);
    }
}
