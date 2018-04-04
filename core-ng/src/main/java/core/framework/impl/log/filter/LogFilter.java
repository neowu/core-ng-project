package core.framework.impl.log.filter;

import core.framework.util.Sets;
import core.framework.util.Strings;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;

/**
 * @author neo
 */
public class LogFilter {
    private static final int MAX_LONG_STRING_SIZE = 15000; // limit long param string to 15k

    static String toString(byte[] bytes, Charset charset, int maxSize) {
        if (bytes == null) return "null";
        if (bytes.length <= maxSize) return new String(bytes, charset);
        StringBuilder builder = new StringBuilder(maxSize + 14);
        String value = new String(bytes, 0, maxSize, charset);
        builder.append(value, 0, value.length() - 1)   // remove the last incomplete char, in utf8, one char takes 1 to 3 bytes
               .append("...(truncated)");   // length = 14
        return builder.toString();
    }

    public final Set<String> maskedFields = Sets.newHashSet();

    public String format(String message, Object[] arguments) {
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
        StringBuilder builder = new StringBuilder(maxSize + 14);
        builder.append(value, 0, maxSize)
               .append("...(truncated)");   // length = 14
        return builder.toString();
    }
}
