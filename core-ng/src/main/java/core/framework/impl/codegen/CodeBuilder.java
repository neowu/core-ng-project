package core.framework.impl.codegen;

import core.framework.api.util.Strings;

/**
 * @author neo
 */
public class CodeBuilder {
    private final StringBuilder builder = new StringBuilder();

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

    @Override
    public String toString() {
        return builder.toString();
    }
}
