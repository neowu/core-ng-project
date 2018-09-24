package core.framework.impl.log.filter;

import core.framework.util.Sets;
import core.framework.util.Strings;

import java.util.Arrays;
import java.util.Set;

/**
 * @author neo
 */
public class LogFilter {
    private static final int MAX_PARAM_SIZE = 10000; // limit long param string to 10k

    public final Set<String> maskedFields = Sets.newHashSet();

    public String format(String message, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return message;    // log message can be null, e.g. message of NPE
        }
        var filteredArguments = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            filteredArguments[i] = filterArgument(arguments[i]);
        }
        return Strings.format(message, filteredArguments);
    }

    private String filterArgument(Object argument) {
        if (argument == null) return null;

        String value;
        if (argument instanceof LogParam) {
            value = ((LogParam) argument).filter(maskedFields);
        } else if (argument.getClass().isArray()) {
            value = filterArrayArgument(argument);
        } else {
            value = String.valueOf(argument);
        }
        return truncate(value, MAX_PARAM_SIZE);
    }

    private String filterArrayArgument(Object argument) {
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

    String truncate(String value, int maxSize) {
        if (value.length() <= maxSize) return value;
        return value.substring(0, maxSize) + "...(truncated)";
    }
}
