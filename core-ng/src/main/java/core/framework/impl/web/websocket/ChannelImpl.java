package core.framework.impl.web.websocket;

import core.framework.log.ActionLogContext;
import core.framework.util.StopWatch;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author neo
 */
public class ChannelImpl implements Channel {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChannelImpl.class);
    private final static ChannelCallback CALLBACK = new ChannelCallback();
    final String action;
    final String clientIP;
    final String refId;
    final ChannelListener listener;
    private final WebSocketChannel channel;
    private final Map<String, Object> context = new ConcurrentHashMap<>();

    ChannelImpl(WebSocketChannel channel, String action, String clientIP, String refId, ChannelListener listener) {
        this.channel = channel;
        this.action = action;
        this.clientIP = clientIP;
        this.refId = refId;
        this.listener = listener;
    }

    @Override
    public void send(String message) {
        var watch = new StopWatch();
        try {
            WebSockets.sendText(message, channel, CALLBACK);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("ws", elapsed, 0, 1);
            LOGGER.debug("send ws message, message={}, elapsed={}", message, elapsed);
        }
    }

    @Override
    public void close() {
        var watch = new StopWatch();
        try {
            WebSockets.sendClose(CloseMessage.NORMAL_CLOSURE, null, channel, CALLBACK);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("ws", elapsed, 0, 1);
            LOGGER.debug("close ws channel, elapsed={}", elapsed);
        }
    }

    @Override
    public Map<String, Object> context() {
        return context;
    }
}
