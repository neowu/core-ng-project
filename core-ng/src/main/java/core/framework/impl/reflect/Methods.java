package core.framework.impl.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class Methods {
    public static String path(Method method) {
        var builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getCanonicalName()).append('.').append(method.getName());
        builder.append('(');
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(paramTypes[i].getTypeName());
        }
        builder.append(')');
        return builder.toString();
    }
}
