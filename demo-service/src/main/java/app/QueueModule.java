package app;

import app.queue.TestMessage;
import app.queue.handle.BulkTestMessageHandler;
import app.queue.web.QueueTestController;
import core.framework.api.Module;

/**
 * @author neo
 */
public class QueueModule extends Module {
    @Override
    protected void initialize() {
        kafka("trace").uri("kafka:9092");
        kafka("trace").publish("k-test-1", TestMessage.class);

        kafka("trace").subscribe("k-test-1", TestMessage.class, bind(BulkTestMessageHandler.class))
                      .poolSize(2);

        route().get("/queue-test", bind(QueueTestController.class));
    }
}
