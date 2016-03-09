package app.product.queue;

import core.framework.api.queue.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class TestMessageHandler implements MessageHandler<TestMessage> {
    private final Logger logger = LoggerFactory.getLogger(TestMessageHandler.class);

    @Override
    public void handle(TestMessage message) throws Exception {

//        logger.warn("here is message {}", message.name);
//        Thread.sleep(5000);
    }
}
