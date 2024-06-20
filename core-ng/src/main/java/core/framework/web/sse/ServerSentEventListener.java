package core.framework.web.sse;

import core.framework.web.Request;

public interface ServerSentEventListener<T> {
    void onConnect(Request request, ServerSentEventChannel<T> channel, String lastEventId);
}
