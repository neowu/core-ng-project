package core.framework.internal.reflect;

import java.lang.annotation.Annotation;

/**
 * @author neo
 */
public final class Params {
    public static <T extends Annotation> T annotation(Annotation[] paramAnnotations, Class<T> annotationClass) {
        for (Annotation annotation : paramAnnotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                @SuppressWarnings("unchecked")
                T result = (T) annotation;
                return result;
            }
        }
        return null;
    }
}
