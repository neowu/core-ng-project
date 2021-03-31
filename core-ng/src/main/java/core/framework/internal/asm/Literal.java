package core.framework.internal.asm;

import core.framework.internal.reflect.GenericTypes;
import core.framework.util.Types;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class Literal {
    public static String type(Class<?> value) {
        return value.getCanonicalName();
    }

    public static String variable(Enum<?> value) {
        return value.getDeclaringClass().getCanonicalName() + "." + value.name();
    }

    public static String variable(Type type) {
        if (GenericTypes.isList(type)) {
            return Types.class.getCanonicalName() + ".list(" + GenericTypes.listValueClass(type).getCanonicalName() + ".class)";
        } else if (GenericTypes.isOptional(type)) {
            return Types.class.getCanonicalName() + ".optional(" + GenericTypes.optionalValueClass(type).getCanonicalName() + ".class)";
        } else {
            return GenericTypes.rawClass(type).getCanonicalName() + ".class";
        }
    }

    public static String variable(String text) {
        if (text == null) return "null";

        var builder = new StringBuilder("\"");
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                default -> builder.append(ch);
            }
        }
        builder.append('\"');
        return builder.toString();
    }
}
