package core.framework.internal.kafka;

import core.framework.internal.validate.Validator;
import core.framework.kafka.MessagePublisher;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class MockMessagePublisher<T> implements MessagePublisher<T> {
    private final String topic;
    private final Validator<T> validator;

    public MockMessagePublisher(String topic, Class<T> messageClass) {
        this.topic = topic;
        validator = Validator.of(messageClass);
    }

    @Override
    public void publish(@Nullable String key, T value) {
        publish(topic, key, value);
    }

    @Override
    public void publish(String topic, @Nullable String key, T value) {
        if (topic == null) throw new Error("topic must not be null");
        validator.validate(value, false);
    }
}
