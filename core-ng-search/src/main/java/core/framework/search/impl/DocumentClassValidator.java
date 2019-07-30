package core.framework.search.impl;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.validate.ClassVisitor;
import core.framework.search.Index;

import java.lang.reflect.Field;

/**
 * @author neo
 */
final class DocumentClassValidator implements ClassVisitor {
    private final JSONClassValidator validator;

    DocumentClassValidator(Class<?> documentClass) {
        validator = new JSONClassValidator(documentClass);
    }

    void validate() {
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        validator.visitClass(objectClass, path);
        if (path == null && !objectClass.isAnnotationPresent(Index.class)) {
            throw new Error("class must have @Index, class=" + objectClass.getCanonicalName());
        }
    }

    @Override
    public void visitField(Field field, String parentPath) {
        validator.visitField(field, parentPath);
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
        validator.visitEnum(enumClass);
    }
}
