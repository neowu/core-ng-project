package core.framework.internal.log.filter;

/**
 * @author neo
 */
public class LogParamHelper {
    public static void append(StringBuilder builder, String value, int maxLength) {
        if (value.length() > maxLength) {
            builder.append(value, 0, maxLength)
                .append("...(truncated)");
        } else {
            builder.append(value);
        }
    }

    public static void append(StringBuilder builder, String[] values, int maxLength) {
        int totalMaxLength = builder.length() + maxLength;
        builder.append('[');
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (i > 0) builder.append(", ");
            builder.append(value);

            if (builder.length() > totalMaxLength) {
                builder.setLength(totalMaxLength);
                builder.append("...(truncated)");
                return;
            }
        }
        builder.append(']');
    }
}
