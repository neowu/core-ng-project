package core.framework.internal.kafka;

import core.framework.internal.validate.Validator;
import core.framework.internal.validate.type.JSONClassValidator;

/**
 * @author neo
 */
public class MessageValidator<T> {
    private final Validator validator;

    MessageValidator(Class<T> messageClass) {
        new JSONClassValidator(messageClass).validate();
        validator = new Validator(messageClass);
    }

    public void validate(T message) {
        validator.validate(message, false);
    }
}
