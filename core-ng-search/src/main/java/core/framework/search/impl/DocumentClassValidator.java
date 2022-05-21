package core.framework.search.impl;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.reflect.Fields;
import core.framework.search.Index;

import java.lang.reflect.Field;

/**
 * @author neo
 */
final class DocumentClassValidator extends JSONClassValidator {
    private Object docWithDefaultValue;

    DocumentClassValidator(Class<?> documentClass) {
        super(documentClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        super.visitClass(objectClass, path);

        if (path == null && !objectClass.isAnnotationPresent(Index.class)) {
            throw new Error("document class must have @Index, class=" + objectClass.getCanonicalName());
        }

        try {
            docWithDefaultValue = objectClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    @Override
    public void visitField(Field field, String parentPath) {
        super.visitField(field, parentPath);

        try {
            // document constructed by "new" with default value will break partialUpdate accidentally, due to fields are not null will be updated to db
            if (field.get(docWithDefaultValue) != null)
                throw new Error("document field must not have default value, field=" + Fields.path(field));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
}
