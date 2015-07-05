package core.framework.impl.codegen;

import core.framework.api.util.Types;
import core.framework.impl.type.TypeInspector;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public class TypeHelper {
    final TypeInspector inspector;

    public TypeHelper(Type type) {
        inspector = new TypeInspector(type);
    }

    public String variableValue() {
        if (inspector.isList()) {
            return Types.class.getCanonicalName() + ".list(" + inspector.listValueClass().getCanonicalName() + ".class)";
        } else {
            return inspector.rawClass.getCanonicalName() + ".class";
        }
    }

    public String variableType() {
        if (inspector.rawClass == void.class) return Void.class.getCanonicalName();
        return inspector.rawClass.getCanonicalName();
    }

    public String canonicalName() {
        return inspector.rawClass.getCanonicalName();
    }

    public boolean isVoid() {
        return inspector.rawClass == void.class;
    }
}
