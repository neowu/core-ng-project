package core.framework.impl.kafka;

import core.framework.impl.validate.type.JSONTypeValidator;

/**
 * @author neo
 */
final class MessageClassValidator extends JSONTypeValidator {
    MessageClassValidator(Class<?> messageClass) {
        super(messageClass);
    }
}
