package core.framework.internal.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class Methods {
    public static String path(Method method) {
        var builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getCanonicalName())
                .append('.')
                .append(method.getName())
                .append('(');
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(paramTypeName(paramTypes[i]));
        }
        builder.append(')');
        return builder.toString();
    }

    private static String paramTypeName(Type paramType) {
        // shorten common types
        if (String.class.equals(paramType)) return "String";
        if (Integer.class.equals(paramType)) return "Integer";
        if (Long.class.equals(paramType)) return "Long";
        return paramType.getTypeName();
    }
}
