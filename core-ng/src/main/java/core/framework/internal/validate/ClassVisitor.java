package core.framework.internal.validate;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public interface ClassVisitor {
    default void visitClass(Class<?> objectClass, String path) {
    }

    default void visitField(Field field, String parentPath) {
    }

    default void visitEnum(Class<?> enumClass) {
    }
}
