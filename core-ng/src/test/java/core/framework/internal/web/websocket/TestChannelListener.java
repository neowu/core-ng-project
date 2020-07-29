package core.framework.internal.web.websocket;

import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;

/**
 * @author neo
 */
public class TestChannelListener implements ChannelListener<TestWebSocketMessage, TestWebSocketMessage> {
    @Override
    public void onMessage(Channel<TestWebSocketMessage> channel, TestWebSocketMessage message) {
    }
}
