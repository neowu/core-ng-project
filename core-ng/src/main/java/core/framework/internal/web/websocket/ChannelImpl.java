package core.framework.internal.web.websocket;

import core.framework.internal.web.bean.ResponseBeanMapper;
import core.framework.log.ActionLogContext;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class ChannelImpl implements Channel, Channel.Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelImpl.class);
    final String id = UUID.randomUUID().toString();
    final Set<String> rooms = Sets.newConcurrentHashSet();
    final ChannelListener listener;
    private final ResponseBeanMapper mapper;
    private final WebSocketChannel channel;
    private final Map<String, Object> context = new ConcurrentHashMap<>();
    private final WebSocketContextImpl webSocketContext;
    String action;
    String clientIP;
    String refId;

    ChannelImpl(WebSocketChannel channel, WebSocketContextImpl webSocketContext, ChannelListener listener, ResponseBeanMapper mapper) {
        this.channel = channel;
        this.webSocketContext = webSocketContext;
        this.listener = listener;
        this.mapper = mapper;
    }

    @Override
    public void send(String message) {
        var watch = new StopWatch();
        try {
            WebSockets.sendText(message, channel, ChannelCallback.INSTANCE);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("ws", elapsed, 0, 1);
            LOGGER.debug("send ws message, id={}, message={}, elapsed={}", id, message, elapsed);
        }
    }

    @Override
    public void send(Object bean) {
        send(new String(mapper.toJSON(bean), UTF_8));
    }

    @Override
    public void close() {
        var watch = new StopWatch();
        try {
            WebSockets.sendClose(CloseMessage.NORMAL_CLOSURE, null, channel, ChannelCallback.INSTANCE);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("ws", elapsed, 0, 1);
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
