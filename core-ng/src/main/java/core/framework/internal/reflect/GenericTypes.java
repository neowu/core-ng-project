package core.framework.internal.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class GenericTypes {
    public static Class<?> rawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof final ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        } else {
            throw new Error("unsupported type, type=" + type);
        }
    }

    public static boolean isList(Type type) {
        return List.class.isAssignableFrom(rawClass(type));
    }

    public static boolean isGenericList(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            return List.class.isAssignableFrom(rawClass) && parameterizedType.getActualTypeArguments()[0] instanceof Class;
        }
        return false;
    }

    public static Class<?> listValueClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public static boolean isOptional(Type type) {
        return Optional.class.equals(rawClass(type));
    }

    public static boolean isGenericOptional(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            return Optional.class.equals(rawClass) && parameterizedType.getActualTypeArguments()[0] instanceof Class;
        }
        return false;
    }

    public static Class<?> optionalValueClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public static boolean isMap(Type type) {
        return Map.class.isAssignableFrom(rawClass(type));
    }

    public static boolean isGenericMap(Type type) {
        if (!(type instanceof ParameterizedType)) return false;

        Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
        return arguments[0] instanceof Class && (arguments[1] instanceof Class || isGenericList(arguments[1]));
    }

    public static Class<?> mapKeyClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public static Type mapValueType(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments()[1];
    }
}
