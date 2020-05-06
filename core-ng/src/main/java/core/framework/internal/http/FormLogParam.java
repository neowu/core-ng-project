package core.framework.internal.http;

import core.framework.internal.log.filter.LogParam;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class FormLogParam implements LogParam {
    private final byte[] bytes;

    public FormLogParam(byte[] bytes) {
        this.bytes = bytes;
    }

    // to reduce unnecessary overhead, assume utf-8 and key are all ascii chars, and each key only appear once in form
    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        boolean truncate = false;
        String value;
        if (bytes.length > maxParamLength) {
            value = new String(bytes, 0, maxParamLength, UTF_8);
            truncate = true;
        } else {
            value = new String(bytes, UTF_8);
        }
        if (shouldMask(value, maskedFields)) {
            builder.append(filter(value, maskedFields));
        } else {
            if (truncate) {
                builder.append(value, 0, value.length() - 1);   // remove last char to void half byte char when cutoff
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
            int index = builder.indexOf(maskedField + '=');
            if (index == -1) continue;
            int start = index + maskedField.length() + 1;
            int end = maskEnd(builder, start);
            builder.replace(start, end, "******");
        }
        return builder;
    }

    private int maskEnd(StringBuilder builder, int start) {
        int end = builder.indexOf("&", start);
        if (end == -1) return builder.length();
        return end;
    }

    private boolean shouldMask(String value, Set<String> maskedFields) {
        for (String maskedField : maskedFields) {
            int index = value.indexOf(maskedField + '=');
            if (index >= 0) return true;
        }
        return false;
    }
}
