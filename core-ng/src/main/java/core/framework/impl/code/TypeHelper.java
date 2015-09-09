package core.framework.impl.code;

import core.framework.api.util.Types;
import core.framework.impl.reflect.GenericTypes;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public class TypeHelper {
    final Type type;

    public TypeHelper(Type type) {
        this.type = type;
    }

    public String variableValue() {
        if (GenericTypes.isList(type)) {
            return Types.class.getCanonicalName() + ".list(" + GenericTypes.listValueClass(type).getCanonicalName() + ".class)";
        } else {
            return GenericTypes.rawClass(type).getCanonicalName() + ".class";
        }
    }

    public String variableType() {
        if (type == void.class) return Void.class.getCanonicalName();
        return GenericTypes.rawClass(type).getCanonicalName();
    }

    public String canonicalName() {
        return GenericTypes.rawClass(type).getCanonicalName();
    }

    public boolean isVoid() {
        return type == void.class;
    }
}
