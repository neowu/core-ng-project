package core.framework.web.websocket;

import core.framework.web.Request;

/**
 * @author neo
 */
public interface ChannelListener<T, V> {
    default void onConnect(Request request, Channel<V> channel) {
    }

    void onMessage(Channel<V> channel, T message);

    default void onClose(Channel<V> channel, int code, String reason) {
    }
}
