package core.framework.impl.code;

import core.framework.api.util.Strings;
import core.framework.api.util.Types;
import core.framework.impl.reflect.GenericTypes;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public class CodeBuilder {
    public static String typeVariableLiteral(Type type) {
        if (GenericTypes.isList(type)) {
            return Types.class.getCanonicalName() + ".list(" + GenericTypes.listValueClass(type).getCanonicalName() + ".class)";
        } else {
            return GenericTypes.rawClass(type).getCanonicalName() + ".class";
        }
    }

    private final StringBuilder builder = new StringBuilder(256);

    public CodeBuilder append(String text) {
        builder.append(text);
        return this;
    }

    public CodeBuilder append(String pattern, Object... argument) {
        builder.append(Strings.format(pattern, argument));
        return this;
    }

    public CodeBuilder indent(int indent) {
        for (int i = 0; i < indent; i++)
            builder.append("    ");
        return this;
    }

    public String build() {
        return builder.toString();
    }
}
