package core.framework.impl.log.filter;

import core.framework.util.Exceptions;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.util.Arrays;
import java.util.Set;

/**
 * @author neo
 */
public class LogFilter {
    private static final int MAX_LONG_STRING_SIZE = 15000; // limit long param string to 15k

    public final Set<String> maskedFields = Sets.newHashSet();

    public String format(String message, Object... arguments) {
        if (arguments == null) {
            return message;    // log message can be null, e.g. message of NPE
        }

        Object[] filteredArguments = Arrays.stream(arguments).map(this::filterParam).toArray();
        return Strings.format(message, filteredArguments);
    }

    private String filterParam(Object argument) {
        if (argument == null) return null;

        String value;
        if (argument instanceof FilterParam) {
            return ((FilterParam) argument).filter(maskedFields);
        } else if (argument.getClass().isArray()) {
            return filterArrayParam(argument);
        } else {
            value = String.valueOf(argument);
        }
        return truncate(value, MAX_LONG_STRING_SIZE);
    }

    private String filterArrayParam(Object argument) {
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
        } else if (argument instanceof short[]) {
            return Arrays.toString((short[]) argument);
        } else if (argument instanceof float[]) {
            return Arrays.toString((float[]) argument);
        }
        throw Exceptions.error("unknown array type, argumentClass={}", argument.getClass().getCanonicalName());
    }

    String truncate(String value, int maxSize) {
        if (value.length() <= maxSize) return value;
        return value.substring(0, maxSize) + "...(truncated)";
    }
}
