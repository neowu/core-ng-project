package core.framework.impl.log.filter;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class BytesLogParam implements LogParam {
    private final byte[] bytes;

    public BytesLogParam(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields) {
        append(builder, MAX_PARAM_LENGTH);
    }

    void append(StringBuilder builder, int maxLength) {
        if (bytes == null) {
            builder.append("null");
        } else if (bytes.length > maxLength) {
            var value = new String(bytes, 0, maxLength, UTF_8);
            builder.append(value, 0, value.length() - 1);   // not use last char as can be cut off bytes
            builder.append("...(truncated)");
        } else {
            builder.append(new String(bytes, UTF_8));
        }
    }
}
