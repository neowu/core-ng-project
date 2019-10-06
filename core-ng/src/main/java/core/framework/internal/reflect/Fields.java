package core.framework.internal.reflect;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public final class Fields {
    public static String path(Field field) {
        return field.getDeclaringClass().getTypeName() + "." + field.getName();
    }
}
