package core.framework.search.impl;

import core.framework.internal.json.JSONClassValidator;
import core.framework.search.Index;

/**
 * @author neo
 */
final class DocumentClassValidator extends JSONClassValidator {
    DocumentClassValidator(Class<?> documentClass) {
        super(documentClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (path == null && !objectClass.isAnnotationPresent(Index.class)) {
            throw new Error("class must have @Index, class=" + objectClass.getCanonicalName());
        }
    }
}
