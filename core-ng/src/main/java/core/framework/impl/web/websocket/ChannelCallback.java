package core.framework.impl.web.websocket;

import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class ChannelCallback implements WebSocketCallback<Void> {
    static final ChannelCallback INSTANCE = new ChannelCallback();
    private final Logger logger = LoggerFactory.getLogger(ChannelCallback.class);

    @Override
    public void complete(WebSocketChannel channel, Void context) {
    }

    @Override
    public void onError(WebSocketChannel channel, Void context, Throwable exception) {
        logger.warn(exception.getMessage(), exception);
    }
}
