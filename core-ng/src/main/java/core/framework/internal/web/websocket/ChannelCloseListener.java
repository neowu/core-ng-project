package core.framework.internal.web.websocket;

import io.undertow.websockets.core.WebSocketChannel;

import static core.framework.internal.web.websocket.WebSocketHandler.CHANNEL_KEY;

/**
 * @author neo
 */
class ChannelCloseListener implements org.xnio.ChannelListener<WebSocketChannel> {
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
        context.remove(wrapper);
    }
}
