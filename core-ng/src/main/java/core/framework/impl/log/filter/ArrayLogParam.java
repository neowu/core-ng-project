package core.framework.impl.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public class ArrayLogParam implements LogParam {
    private final String[] values;

    public ArrayLogParam(String... values) {
        this.values = values;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        int maxLength = builder.length() + maxParamLength;
        builder.append('[');
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (i > 0) builder.append(", ");
            builder.append(value);

            if (builder.length() >= maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                return;
            }
        }
        builder.append(']');
    }
}
