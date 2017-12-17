package core.framework.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        @Override
        public int hashCode() {
            return Arrays.hashCode(arguments) ^ rawType.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ParameterizedType)) {
                return false;
            }
            ParameterizedType that = (ParameterizedType) other;
            return rawType.equals(that.getRawType())
                && that.getOwnerType() == null
                && Arrays.equals(arguments, that.getActualTypeArguments());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(rawType.getTypeName())
                .append('<');

            int i = 0;
            for (Type argument : arguments) {
                if (i > 0) builder.append(", ");
                builder.append(argument.getTypeName());
                i++;
            }

            builder.append('>');
            return builder.toString();
        }
    }
}
