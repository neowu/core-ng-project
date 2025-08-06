package core.framework.internal.validate;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public interface ClassVisitor {
    default void visitClass(Class<?> objectClass, @Nullable String path) {
    }

    default void visitField(Field field, @Nullable String parentPath) {
    }

    default void visitEnum(Class<?> enumClass) {
    }
}
