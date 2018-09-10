package core.framework.impl.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class GenericTypes {
    public static Class<?> rawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            throw new Error(format("unsupported type, type={}", type));
        }
    }

    public static boolean isList(Type type) {
        return List.class.isAssignableFrom(rawClass(type));
    }

    public static boolean isGenericList(Type type) {
        if (!(type instanceof ParameterizedType)) return false;

        Class<?> rawClass = (Class<?>) ((ParameterizedType) type).getRawType();
        return List.class.isAssignableFrom(rawClass) && ((ParameterizedType) type).getActualTypeArguments()[0] instanceof Class;
    }

    public static Class<?> listValueClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public static boolean isOptional(Type type) {
        return Optional.class.equals(rawClass(type));
    }

    public static boolean isGenericOptional(Type type) {
        if (!(type instanceof ParameterizedType)) return false;

        Class<?> rawClass = (Class<?>) ((ParameterizedType) type).getRawType();
        return Optional.class.equals(rawClass) && ((ParameterizedType) type).getActualTypeArguments()[0] instanceof Class;
    }

    public static Class<?> optionalValueClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public static boolean isMap(Type type) {
        return Map.class.isAssignableFrom(rawClass(type));
    }

    public static boolean isGenericStringMap(Type type) {
        if (!(type instanceof ParameterizedType)) return false;

        Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
        if (!(keyType instanceof Class)) return false;
        Class<?> keyClass = (Class<?>) keyType;
        return String.class.equals(keyClass) && ((ParameterizedType) type).getActualTypeArguments()[1] instanceof Class;
    }

    public static Class<?> mapValueClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[1];
    }
}
