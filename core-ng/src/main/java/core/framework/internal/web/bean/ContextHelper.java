package core.framework.internal.web.bean;

import core.framework.internal.reflect.GenericTypes;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
class ContextHelper {
    static <T, V> T context(Map<Class<?>, ?> context, Class<V> beanClass) {
        @SuppressWarnings("unchecked")
        T result = (T) context.get(beanClass);
        if (result == null) {
            if (beanClass.getPackageName().startsWith("java")) {   // provide better error message for developer, rather than return class is not registered message
                throw new Error("bean class must not be java built-in class, class=" + beanClass.getCanonicalName());
            }
            throw new Error("bean class is not registered, please use http().bean() to register, class=" + beanClass.getCanonicalName());
        }
        return result;
    }

    static Class<?> responseBeanClass(Type responseType) {
        return GenericTypes.isOptional(responseType) ? GenericTypes.optionalValueClass(responseType) : GenericTypes.rawClass(responseType);
    }
}
