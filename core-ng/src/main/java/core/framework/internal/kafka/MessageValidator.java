package core.framework.internal.kafka;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.validate.Validator;

/**
 * @author neo
 */
public class MessageValidator<T> {
    private final Validator validator;

    MessageValidator(Class<T> messageClass) {
        new JSONClassValidator(messageClass).validate();
        validator = Validator.of(messageClass);
    }

    public void validate(T message) {
        validator.validate(message, false);
    }
}
