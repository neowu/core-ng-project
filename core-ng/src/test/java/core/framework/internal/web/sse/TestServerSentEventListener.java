package core.framework.internal.web.sse;

import core.framework.web.Request;
import core.framework.web.sse.ServerSentEventChannel;
import core.framework.web.sse.ServerSentEventListener;

public class TestServerSentEventListener implements ServerSentEventListener<TestEvent> {
    @Override
    public void onConnect(Request request, ServerSentEventChannel<TestEvent> channel, String lastEventId) {

    }
}
