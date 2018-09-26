package core.framework.impl.log.filter;

import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author neo
 */
public class JSONLogParam implements LogParam {
    private final byte[] bytes;
    private final Charset charset;

    public JSONLogParam(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        boolean truncate = false;
        String value;
        if (bytes.length > maxParamLength) {
            value = new String(bytes, 0, maxParamLength, charset);
            truncate = true;
        } else {
            value = new String(bytes, charset);
        }
        if (shouldMask(value, maskedFields)) {
            builder.append(filter(value, maskedFields));
        } else {
            if (truncate) {
                builder.append(value, 0, value.length() - 1);
            } else {
                builder.append(value);
            }
        }
        if (truncate) {
            builder.append("...(truncated)");
        }
    }

    StringBuilder filter(String value, Set<String> maskedFields) {
        var builder = new StringBuilder(value);
        for (String maskedField : maskedFields) {
            int current = -1;
            while (true) {
                current = builder.indexOf('\"' + maskedField + '\"', current);
                if (current < 0) break;
                int[] range = maskRange(builder, current + maskedField.length() + 2);    // start from last double quote
                if (range == null) break;
                builder.replace(range[0], range[1], "******");      // with benchmark, StringBuilder.replace is the fastest way to mask substring
                current = range[1];
            }
        }
        return builder;
    }

    private int[] maskRange(StringBuilder builder, int start) {  // find first json "string" range from start
        boolean escaped = false;
        int maskStart = -1;
        int length = builder.length();
        for (int index = start; index < length; index++) {
            char ch = builder.charAt(index);
            if (ch == '\\') {
                escaped = true;
            } else if (!escaped && maskStart < 0 && ch == '\"') {
                maskStart = index + 1;
            } else if (!escaped && maskStart >= 0 && ch == '\"') {
                return new int[]{maskStart, index};
            } else {
                escaped = false;
            }
        }
        return null;
    }

    private boolean shouldMask(String value, Set<String> maskedFields) {
        for (String maskedField : maskedFields) {
            int index = value.indexOf('\"' + maskedField + '\"');
            if (index >= 0) return true;
        }
        return false;
    }
}
