package core.framework.search.impl;

import core.framework.impl.validate.type.JSONClassValidator;
import core.framework.search.Index;

import static core.framework.util.Strings.format;

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
            throw new Error(format("class must have @Index, class={}", objectClass.getCanonicalName()));
        }
    }
}
