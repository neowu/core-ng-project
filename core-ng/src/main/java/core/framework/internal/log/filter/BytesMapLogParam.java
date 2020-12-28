package core.framework.internal.log.filter;

import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class BytesMapLogParam implements LogParam {
    private final Map<String, byte[]> values;

    public BytesMapLogParam(Map<String, byte[]> values) {
        this.values = values;
    }

    // not masking the bytes values, thought it can be in json format
    // currently sensitive info won't be handled in batch via cache/redis, so not to add unnecessary overhead
    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        int maxLength = builder.length() + maxParamLength;
        builder.append('{');
        int index = 0;
        for (Map.Entry<String, byte[]> entry : values.entrySet()) {
            if (index > 0) builder.append(", ");
            builder.append(entry.getKey())
                    .append('=')
                    .append(new String(entry.getValue(), UTF_8));

            if (builder.length() > maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                return;
            }

            index++;
        }
        builder.append('}');
    }
}
