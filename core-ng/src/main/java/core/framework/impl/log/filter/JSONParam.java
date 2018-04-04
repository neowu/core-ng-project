package core.framework.impl.log.filter;

import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author neo
 */
public class JSONParam implements FilterParam {
    private final byte[] bytes;
    private final Charset charset;

    public JSONParam(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public String filter(Set<String> maskedFields) {
        String value = new String(bytes, charset);

        int firstMaskIndex = findMaskIndex(value, maskedFields, 0);
        if (firstMaskIndex == -1) return value;

        int currentIndex = 0;
        int maskIndex = firstMaskIndex;
        StringBuilder builder = new StringBuilder();
        while (true) {
            builder.append(value, currentIndex, maskIndex);
            currentIndex = mask(value, builder, maskIndex);
            maskIndex = findMaskIndex(value, maskedFields, currentIndex);
            if (maskIndex == -1) {
                builder.append(value, currentIndex, value.length());
                break;
            }
        }
        return builder.toString();
    }

    private int mask(String value, StringBuilder builder, int maskIndex) {
        boolean masked = false;
        boolean escaped = false;
        int length = value.length();
        int index;
        for (index = maskIndex; index < length; index++) {
            char ch = value.charAt(index);
            if (ch == '\\') {
                escaped = true;
            } else if (!escaped && !masked && ch == '\"') {
                masked = true;
                escaped = false;
                builder.append(ch);
            } else if (!escaped && masked && ch == '\"') {
                builder.append("******");
                break;
            } else {
                escaped = false;
                if (!masked) {
                    builder.append(ch);
                }
            }
        }
        return index;
    }

    private int findMaskIndex(String value, Set<String> maskedFields, int fromIndex) {
        int maskIndex = -1;
        for (String maskedField : maskedFields) {
            int index = value.indexOf('\"' + maskedField + '\"', fromIndex);
            if (index >= 0 && (index < maskIndex || maskIndex == -1)) maskIndex = index + maskedField.length() + 2;    // start right after "field"
        }
        return maskIndex;
    }
}
