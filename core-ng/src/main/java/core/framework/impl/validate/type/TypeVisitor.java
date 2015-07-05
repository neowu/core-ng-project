package core.framework.impl.validate.type;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public interface TypeVisitor {
    void visitClass(Class<?> instanceClass, boolean topLevel);

    void visitField(Field field, boolean topLevel);

    void onComplete();
}
