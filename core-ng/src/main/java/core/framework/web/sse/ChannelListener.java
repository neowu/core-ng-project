package core.framework.web.sse;

import core.framework.web.Request;

public interface ChannelListener<T> {
    void onConnect(Request request, Channel<T> channel, String lastEventId);
}
