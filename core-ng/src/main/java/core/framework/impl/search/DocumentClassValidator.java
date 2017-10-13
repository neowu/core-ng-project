package core.framework.impl.search;

import core.framework.api.search.Index;
import core.framework.api.util.Exceptions;
import core.framework.impl.validate.type.JSONTypeValidator;

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
