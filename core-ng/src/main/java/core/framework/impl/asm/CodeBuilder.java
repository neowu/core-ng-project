package core.framework.impl.asm;

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

    public CodeBuilder append(String pattern, Object... argument) {
        builder.append(Strings.format(pattern, argument));
        return this;
    }

    public CodeBuilder appendCommaSeparatedValues(List<String> values) {
        int index = 0;
        for (String value : values) {
            if (index > 0) builder.append(", ");
            builder.append(value);
            index++;
        }
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
