package app.queue.handle;

import app.queue.TestMessage;
import core.framework.api.kafka.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class TestMessageHandler implements MessageHandler<TestMessage> {
    private final Logger logger = LoggerFactory.getLogger(TestMessageHandler.class);

    @Override
    public void handle(String key, TestMessage value) throws Exception {
        logger.warn("debug, key={}, value.name={}", key, value.name);
    }
}
