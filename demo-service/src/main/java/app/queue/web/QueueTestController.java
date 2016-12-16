package app.queue.web;

import app.queue.TestMessage;
import core.framework.api.kafka.MessagePublisher;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author neo
 */
public class QueueTestController implements Controller {
    @Inject
    @Named("trace")
    MessagePublisher<TestMessage> publisher;

    @Override
    public Response execute(Request request) throws Exception {
        for (int i = 0; i < 100; i++) {
            TestMessage value = new TestMessage();
            value.name = "neo-" + i;
            publisher.publish(String.valueOf(i), value);
        }
        return Response.empty();
    }
}
