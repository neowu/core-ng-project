package core.framework.impl.kafka;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.impl.validate.Validator;

import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

/**
 * @author neo
 */
public class MessageValidator {
    private final Map<Class<?>, Validator> validators = Maps.newHashMap();

    public void register(Class<?> messageClass) {
        validators.computeIfAbsent(messageClass, key -> {
            new MessageClassValidator(messageClass).validate();
            return new Validator(key, field -> field.getDeclaredAnnotation(XmlElement.class).name());
        });
    }

    public <T> void validate(T message) {
        if (message == null) throw new Error("message must not be null");

        Validator validator = validators.get(message.getClass());
        if (validator == null)
            throw Exceptions.error("message class is not registered, class={}", message.getClass().getCanonicalName());

        validator.validate(message);
    }
}
