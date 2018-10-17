package core.framework.impl.log.filter;

import java.nio.charset.Charset;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class BytesLogParam implements LogParam {
    private final byte[] bytes;
    private final Charset charset;

    public BytesLogParam(byte[] bytes) {
        this(bytes, UTF_8);
    }

    public BytesLogParam(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        if (bytes == null) {
            builder.append("null");
        } else if (bytes.length > maxParamLength) {
            var value = new String(bytes, 0, maxParamLength, charset);
            builder.append(value, 0, value.length() - 1);   // not use last char as can be cut off bytes
            builder.append("...(truncated)");
        } else {
            builder.append(new String(bytes, charset));
        }
    }
}
