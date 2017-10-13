package core.framework.impl.search;

import core.framework.impl.validate.type.JSONTypeValidator;
import core.framework.search.Index;
import core.framework.util.Exceptions;

/**
 * @author neo
 */
final class DocumentClassValidator extends JSONTypeValidator {
    DocumentClassValidator(Class<?> documentClass) {
        super(documentClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (path == null && !objectClass.isAnnotationPresent(Index.class)) {
            throw Exceptions.error("class must have @Index, class={}", objectClass.getCanonicalName());
        }
    }
}
