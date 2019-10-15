package core.framework.web.websocket;

import core.framework.web.Request;

/**
 * @author neo
 */
public interface ChannelListener {
    default void onConnect(Request request, Channel channel) {
    }

    void onMessage(Channel channel, String message);

    default void onClose(Channel channel, int code, String reason) {
    }
}
