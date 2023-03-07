package core.framework.internal.web.websocket;

import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static core.framework.internal.web.websocket.WebSocketHandler.CHANNEL_KEY;

/**
 * @author neo
 */
class ChannelCloseListener implements org.xnio.ChannelListener<WebSocketChannel> {
    private final Logger logger = LoggerFactory.getLogger(ChannelCloseListener.class);
    private final WebSocketContextImpl context;

    ChannelCloseListener(WebSocketContextImpl context) {
        this.context = context;
    }

    @Override
    public void handleEvent(WebSocketChannel channel) {
        var wrapper = (ChannelImpl<?, ?>) channel.getAttribute(CHANNEL_KEY);
        remove(wrapper);
    }

    void remove(ChannelImpl<?, ?> wrapper) {
        // debug info
        logger.info("close channel, channel={}", wrapper.id);
        context.remove(wrapper);
    }
}
