package core.framework.impl.queue;

import core.framework.api.queue.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class MockMessagePublisher<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(MockMessagePublisher.class);
    private final String uri;
    private final MessageValidator validator;

    public MockMessagePublisher(String uri, MessageValidator validator) {
        this.uri = uri;
        this.validator = validator;
    }

    @Override
    public void publish(T message) {
        reply(uri, message);
    }

    @Override
    public void reply(String uri, T message) {
        logger.info("publish message, uri={}, messageClass={}", uri, message.getClass().getCanonicalName());
        validator.validate(message);
    }
}
