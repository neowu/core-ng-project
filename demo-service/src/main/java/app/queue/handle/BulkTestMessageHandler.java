package app.queue.handle;

import app.queue.TestMessage;
import core.framework.api.kafka.BulkMessageHandler;
import core.framework.api.kafka.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public class BulkTestMessageHandler implements BulkMessageHandler<TestMessage> {
    private final Logger logger = LoggerFactory.getLogger(BulkTestMessageHandler.class);

    @Override
    public void handle(List<Message<TestMessage>> messages) throws Exception {
        logger.warn("debug, message.size={}", messages.size());
        for (Message<TestMessage> message : messages) {
            logger.debug(message.key + " " + message.value.name);
        }
    }
}
