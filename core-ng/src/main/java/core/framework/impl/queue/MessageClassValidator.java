package core.framework.impl.queue;

import core.framework.api.queue.Message;
import core.framework.api.util.Exceptions;
import core.framework.impl.validate.type.JAXBTypeValidator;

/**
 * @author neo
 */
final class MessageClassValidator extends JAXBTypeValidator {
    MessageClassValidator(Class<?> messageClass) {
        super(messageClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (path == null && !objectClass.isAnnotationPresent(Message.class)) {
            throw Exceptions.error("class must have @Message, class={}", objectClass.getCanonicalName());
        }
        super.visitClass(objectClass, path);
    }
}
