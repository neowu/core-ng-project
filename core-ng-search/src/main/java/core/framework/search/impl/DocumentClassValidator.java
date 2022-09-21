package core.framework.search.impl;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.reflect.Fields;
import core.framework.search.Index;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
final class DocumentClassValidator extends JSONClassValidator {
    private final Map<Class<?>, Object> defaultObjects = new HashMap<>();

    DocumentClassValidator(Class<?> documentClass) {
        super(documentClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        super.visitClass(objectClass, path);

        if (path == null && !objectClass.isAnnotationPresent(Index.class)) {
            throw new Error("document class must have @Index, class=" + objectClass.getCanonicalName());
        }
    }

    @Override
    public void visitField(Field field, String parentPath) {
        super.visitField(field, parentPath);

        try {
            Object defaultObject = defaultObject(field);
            // document constructed by "new" with default value will break partialUpdate accidentally, due to fields are not null will be updated to es
            if (field.get(defaultObject) != null)
                throw new Error("document field must not have default value, field=" + Fields.path(field));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private Object defaultObject(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        Object object = defaultObjects.get(declaringClass);
        if (object == null) {
            try {
                object = declaringClass.getDeclaredConstructor().newInstance();
                defaultObjects.put(declaringClass, object);
            } catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }
        return object;
    }
}
