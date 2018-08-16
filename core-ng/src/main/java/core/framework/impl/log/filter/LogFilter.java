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
    private static final int MAX_PARAM_SIZE = 15000; // limit long param string to 15k

    public final Set<String> maskedFields = Sets.newHashSet();

    public String format(String message, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return message;    // log message can be null, e.g. message of NPE
        }
        Object[] filteredArguments = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            filteredArguments[i] = filterParam(arguments[i]);
        }
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
        return truncate(value, MAX_PARAM_SIZE);
    }

    private String filterArrayParam(Object param) {
        if (param instanceof Object[]) {
            return Arrays.toString((Object[]) param);
        } else if (param instanceof int[]) {
            return Arrays.toString((int[]) param);
        } else if (param instanceof long[]) {
            return Arrays.toString((long[]) param);
        } else if (param instanceof char[]) {
            return Arrays.toString((char[]) param);
        } else if (param instanceof double[]) {
            return Arrays.toString((double[]) param);
        } else if (param instanceof byte[]) {
            return Arrays.toString((byte[]) param);
        } else if (param instanceof boolean[]) {
            return Arrays.toString((boolean[]) param);
        } else if (param instanceof short[]) {
            return Arrays.toString((short[]) param);
        } else if (param instanceof float[]) {
            return Arrays.toString((float[]) param);
        }
        throw Exceptions.error("unknown array type, paramClass={}", param.getClass().getCanonicalName());
    }

    String truncate(String value, int maxSize) {
        if (value.length() <= maxSize) return value;
        return value.substring(0, maxSize) + "...(truncated)";
    }
}
