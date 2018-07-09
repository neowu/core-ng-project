package core.framework.impl.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class Methods {
    public static String path(Method method) {
        return method.getDeclaringClass().getCanonicalName() + "." + method.getName()
                + '(' + Arrays.stream(method.getGenericParameterTypes()).map(Type::getTypeName).collect(Collectors.joining(", ")) + ')';
    }
}
