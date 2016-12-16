package core.framework.impl.kafka;

import core.framework.impl.validate.type.JAXBTypeValidator;

/**
 * @author neo
 */
final class MessageClassValidator extends JAXBTypeValidator {
    MessageClassValidator(Class<?> messageClass) {
        super(messageClass);
    }
}
