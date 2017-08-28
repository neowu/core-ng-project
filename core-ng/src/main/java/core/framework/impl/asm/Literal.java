package core.framework.impl.asm;

import core.framework.api.util.Types;
import core.framework.impl.reflect.GenericTypes;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class Literal {
    public static String type(Type type) {
        return GenericTypes.rawClass(type).getCanonicalName();
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
}
