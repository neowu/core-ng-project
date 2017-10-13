package core.framework.impl.kafka;

import core.framework.api.json.Property;
import core.framework.impl.validate.Validator;

/**
 * @author neo
 */
public class MessageValidator<T> {
    private final Validator validator;

    MessageValidator(Class<T> messageClass) {
        new MessageClassValidator(messageClass).validate();
        validator = new Validator(messageClass, field -> field.getDeclaredAnnotation(Property.class).name());
    }

    public void validate(T message) {
        validator.validate(message);
    }
}
