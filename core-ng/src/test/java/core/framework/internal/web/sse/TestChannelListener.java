package core.framework.internal.web.sse;

import core.framework.web.Request;
import core.framework.web.sse.Channel;
import core.framework.web.sse.ChannelListener;

public class TestChannelListener implements ChannelListener<TestEvent> {
    @Override
    public void onConnect(Request request, Channel<TestEvent> channel, String lastEventId) {

    }
}
