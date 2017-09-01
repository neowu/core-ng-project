package core.framework.impl.kafka;

import core.framework.impl.validate.Validator;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
public class MessageValidator<T> {
    private final Validator validator;

    MessageValidator(Class<T> messageClass) {
        new MessageClassValidator(messageClass).validate();
        validator = new Validator(messageClass, field -> field.getDeclaredAnnotation(XmlElement.class).name());
    }

    public void validate(T message) {
        validator.validate(message);
    }
}
