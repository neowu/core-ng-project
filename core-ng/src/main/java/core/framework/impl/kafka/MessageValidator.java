package core.framework.impl.kafka;

import core.framework.api.json.Property;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.type.JSONClassValidator;

/**
 * @author neo
 */
public class MessageValidator<T> {
    private final Validator validator;

    MessageValidator(Class<T> messageClass) {
        new JSONClassValidator(messageClass).validate();
        validator = new Validator(messageClass, field -> field.getDeclaredAnnotation(Property.class).name());
    }

    public void validate(T message) {
        validator.validate(message, false);
    }
}
