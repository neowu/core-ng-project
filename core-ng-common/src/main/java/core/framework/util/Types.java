package core.framework.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author neo
 */
public final class Types {
    public static Type generic(Class<?> rawType, Type... arguments) {
        return new ParameterizedTypeImpl(rawType, arguments);
    }

    public static Type optional(Type valueType) {
        return generic(Optional.class, valueType);
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

    static final class ParameterizedTypeImpl implements ParameterizedType {
        private final Type rawType;
        private final Type[] arguments;

        ParameterizedTypeImpl(Class<?> rawType, Type... arguments) {
            this.rawType = rawType;
            this.arguments = arguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return arguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        // refer to sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl.hashCode, must return same hashcode as builtin type
        @Override
        public int hashCode() {
            return Arrays.hashCode(arguments) ^ rawType.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ParameterizedType)) return false;

            ParameterizedType that = (ParameterizedType) other;
            return Objects.equals(rawType, that.getRawType())
                    && that.getOwnerType() == null
                    && Arrays.equals(arguments, that.getActualTypeArguments());
        }

        @Override
        public String toString() {
            var builder = new StringBuilder(rawType.getTypeName()).append('<');

            for (int i = 0; i < arguments.length; i++) {
                if (i > 0) builder.append(", ");
                builder.append(arguments[i].getTypeName());
            }

            builder.append('>');
            return builder.toString();
        }
    }
}
