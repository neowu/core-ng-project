package core.framework.internal.kafka;

import core.framework.internal.validate.Validator;
import core.framework.kafka.MessagePublisher;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class MockMessagePublisher<T> implements MessagePublisher<T> {
    private final Validator<T> validator;

    public MockMessagePublisher(Class<T> messageClass) {
        validator = Validator.of(messageClass);
    }

    @Override
    public void publish(@Nullable String key, T value) {
        validator.validate(value, false);
    }
}
