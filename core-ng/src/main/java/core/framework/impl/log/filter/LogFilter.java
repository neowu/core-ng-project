package core.framework.impl.log.filter;

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
        String value;
        if (argument instanceof FilterParam) {
            value = ((FilterParam) argument).filter(maskedFields);
        } else {
            value = String.valueOf(argument);
        }
        return truncate(value, MAX_LONG_STRING_SIZE);
    }

    String truncate(String value, int maxSize) {
        if (value.length() <= maxSize) return value;
        return value.substring(0, maxSize) + "...(truncated)";
    }
}
