package core.framework.web.websocket;

import core.framework.web.Request;

/**
 * @author neo
 */
public interface ChannelListener<T> {
    default void onConnect(Request request, Channel channel) {
    }

    void onMessage(Channel channel, T message);

    default void onClose(Channel channel, int code, String reason) {
    }
}
