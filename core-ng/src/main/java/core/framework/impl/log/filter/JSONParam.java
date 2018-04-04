package core.framework.impl.log.filter;

import core.framework.util.Charsets;

import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author neo
 */
public class JSONParam implements FilterParam {
    private final byte[] bytes;
    private final Charset charset;

    public JSONParam(byte[] bytes) {
        this(bytes, Charsets.UTF_8);
    }

    public JSONParam(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public String filter(Set<String> maskedFields) {
        String value = new String(bytes, charset);

        int firstIndex = nextMaskIndex(value, maskedFields, 0);
        if (firstIndex == -1) return value;

        int currentIndex = 0;
        int maskIndex;
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (currentIndex == 0) maskIndex = firstIndex;
            else maskIndex = nextMaskIndex(value, maskedFields, currentIndex);

            if (maskIndex == -1) {
                builder.append(value, currentIndex, value.length());
                break;
            }

            currentIndex = mask(builder, value, currentIndex, maskIndex);
        }
        return builder.toString();
    }

    private int mask(StringBuilder builder, String value, int start, int maskPosition) {
        builder.append(value, start, maskPosition);
        boolean masked = false;
        boolean escaped = false;
        int length = value.length();
        int index;
        for (index = maskPosition; index < length; index++) {
            char c = value.charAt(index);
            if (c == '\\') {
                escaped = true;
            } else if (!escaped && !masked && c == '\"') {
                masked = true;
                escaped = false;
                builder.append(c);
            } else if (!escaped && masked && c == '\"') {
                builder.append("******");
                break;
            } else {
                escaped = false;
                if (!masked) {
                    builder.append(c);
                }
            }
        }
        return index;
    }

    private int nextMaskIndex(String value, Set<String> maskedFields, int fromIndex) {
        int position = -1;
        for (String maskedField : maskedFields) {
            int index = value.indexOf('\"' + maskedField + '\"', fromIndex);
            if (index >= 0 && (index < position || position == -1)) position = index + maskedField.length() + 2;    // start right after "field"
        }
        return position;
    }

    private String mask(String source, String reg) {
        int start = source.indexOf(reg);
        StringBuilder builder = new StringBuilder();
        builder.append(source, 0, start + reg.length());
        boolean masked = false;
        boolean escaped = false;
        int i;

        for (i = start + reg.length(); i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\\') {
                escaped = true;
            } else if (!escaped && !masked && c == '\"') {
                masked = true;
                escaped = false;
                builder.append(c);
            } else if (!escaped && masked && c == '\"') {
                builder.append("masked");
                break;
            } else {
                escaped = false;
                if (!masked) {
                    builder.append(c);
                }
            }
        }

        if (i < source.length()) {
            builder.append(source, i, source.length());
        }
        return builder.toString();
    }
}
