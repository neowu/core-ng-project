package core.framework.api.util;

import core.framework.impl.type.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author neo
 */
public final class Types {
    public static Type generic(Class<?> rawType, Type... arguments) {
        return new ParameterizedTypeImpl(rawType, arguments, null);
    }

    public static Type list(Type valueType) {
        return generic(List.class, valueType);
    }

    public static Type map(Type keyType, Type valueType) {
        return generic(Map.class, keyType, valueType);
    }

    public static Type supplier(Type valueType) {
        return generic(Supplier.class, valueType);
    }
}
