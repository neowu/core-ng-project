package core.framework.internal.log.filter;

import core.framework.util.Sets;

import java.util.Arrays;
import java.util.Set;

/**
 * @author neo
 */
public class LogFilter {
    private static final int MAX_PARAM_LENGTH = 10000; // limit long param string to 10k
    public final Set<String> maskedFields = Sets.newHashSet();

    public void append(StringBuilder builder, String message, Object... arguments) {
        if (message == null || arguments == null) {
            builder.append(message);
            return;
        }
        int position = 0;
        for (Object argument : arguments) {
            int index = message.indexOf("{}", position);
            if (index == -1) {
                builder.append(message, position, message.length());    // there are more arguments than count of "{}"
                return;
            } else {
                builder.append(message, position, index);
                appendArgument(builder, argument);
                position = index + 2;
            }
        }
        if (position < message.length()) {
            builder.append(message, position, message.length());
        }
    }

    private void appendArgument(StringBuilder builder, Object argument) {
        if (argument == null) {
            builder.append("null");
            return;
        }
        // for performance reason, truncation will be handled by each type of LogParam, to best fit each specific cases
        if (argument instanceof LogParam logParam) {
            logParam.append(builder, maskedFields, MAX_PARAM_LENGTH);
            return;
        }
        appendRawArgument(builder, argument, MAX_PARAM_LENGTH);
    }

    void appendRawArgument(StringBuilder builder, Object argument, int maxLength) {
        String value;
        if (argument.getClass().isArray()) {
            value = arrayArgument(argument);
        } else {
            value = String.valueOf(argument);
        }
        LogParamHelper.append(builder, value, maxLength);
    }

    private String arrayArgument(Object argument) {
        return switch (argument) {
            case final Object[] value -> Arrays.toString(value);
            case final int[] value -> Arrays.toString(value);
            case final long[] value -> Arrays.toString(value);
            case final char[] value -> Arrays.toString(value);
            case final double[] value -> Arrays.toString(value);
            case final byte[] value -> Arrays.toString(value);
            case final boolean[] value -> Arrays.toString(value);
            case final float[] value -> Arrays.toString(value);
            case final short[] value -> Arrays.toString(value);
            case null -> "null";
            default -> throw new Error("unexpected argument, argument=" + argument);
        };
    }
}
