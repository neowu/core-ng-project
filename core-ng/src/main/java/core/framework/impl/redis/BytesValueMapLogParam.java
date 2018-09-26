package core.framework.impl.redis;

import core.framework.impl.log.filter.LogParam;

import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
class BytesValueMapLogParam implements LogParam {
    private final Map<String, byte[]> values;

    BytesValueMapLogParam(Map<String, byte[]> values) {
        this.values = values;
    }

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

            if (builder.length() >= maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                return;
            }

            index++;
        }
        builder.append('}');
    }
}
