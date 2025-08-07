package core.framework.internal.asm;

import core.framework.util.Strings;

import java.util.List;

/**
 * @author neo
 */
public class CodeBuilder {
    private final StringBuilder builder = new StringBuilder(256);

    public CodeBuilder append(String text) {
        builder.append(text);
        return this;
    }

    public CodeBuilder append(char ch) {
        builder.append(ch);
        return this;
    }

    public CodeBuilder append(Object object) {
        builder.append(object);
        return this;
    }

    public CodeBuilder append(String pattern, Object... params) {
        builder.append(Strings.format(pattern, params));
        return this;
    }

    public CodeBuilder appendCommaSeparatedValues(List<String> values) {
        String valuesString = String.join(", ", values);
        builder.append(valuesString);
        return this;
    }

    public CodeBuilder indent(int indent) {
        builder.append("    ".repeat(Math.max(0, indent)));
        return this;
    }

    public String build() {
        return builder.toString();
    }
}
