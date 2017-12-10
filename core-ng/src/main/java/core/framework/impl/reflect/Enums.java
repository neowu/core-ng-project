package core.framework.impl.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author neo
 */
public class Enums {
    public static <T extends Enum<?>, A extends Annotation> A constantAnnotation(T constant, Class<A> annotationClass) {
        try {
            Field field = constant.getDeclaringClass().getField(constant.name());
            return field.getDeclaredAnnotation(annotationClass);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public static <T extends Enum<?>> String path(T constant) {
        return constant.getDeclaringClass().getTypeName() + "." + constant.name();
    }
}
