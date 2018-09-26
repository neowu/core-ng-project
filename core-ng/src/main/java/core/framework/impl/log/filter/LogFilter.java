package core.framework.impl.log.filter;

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
        if (argument instanceof LogParam) {
            ((LogParam) argument).append(builder, maskedFields, MAX_PARAM_LENGTH);
            return;
        }
        String value;
        if (argument.getClass().isArray()) {
            value = arrayArgument(argument);
        } else {
            value = String.valueOf(argument);
        }
        truncate(builder, value, MAX_PARAM_LENGTH);
    }

    private String arrayArgument(Object argument) {
        if (argument instanceof Object[]) {
            return Arrays.toString((Object[]) argument);
        } else if (argument instanceof int[]) {
            return Arrays.toString((int[]) argument);
        } else if (argument instanceof long[]) {
            return Arrays.toString((long[]) argument);
        } else if (argument instanceof char[]) {
            return Arrays.toString((char[]) argument);
        } else if (argument instanceof double[]) {
            return Arrays.toString((double[]) argument);
        } else if (argument instanceof byte[]) {
            return Arrays.toString((byte[]) argument);
        } else if (argument instanceof boolean[]) {
            return Arrays.toString((boolean[]) argument);
        } else if (argument instanceof float[]) {
            return Arrays.toString((float[]) argument);
        } else {    // in java there are only those possible array type, the last one is short[]
            return Arrays.toString((short[]) argument);
        }
    }

    void truncate(StringBuilder builder, String value, int maxLength) {
        if (value.length() > maxLength) {
            builder.append(value, 0, maxLength);
            builder.append("...(truncated)");
        } else {
            builder.append(value);
        }
    }
}
