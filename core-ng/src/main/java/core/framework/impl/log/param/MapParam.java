package core.framework.impl.log.param;

import core.framework.util.Charsets;

import java.util.Map;

/**
 * @author neo
 */
public class MapParam {
    private final Map<?, ?> map;

    public MapParam(Map<?, ?> map) {
        this.map = map;
    }

    // replicate AbstractMap.toString with encoding
    @Override
    public String toString() {
        if (map == null) return "null";
        StringBuilder builder = new StringBuilder();
        int index = 0;
        builder.append('{');
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (index > 0) builder.append(", ");
            Object key = entry.getKey();
            Object value = entry.getValue();
            builder.append(encode(key));
            builder.append('=');
            builder.append(encode(value));
            index++;
        }
        return builder.append('}').toString();
    }

    private String encode(Object value) {
        if (value instanceof byte[]) return LogParamHelper.toString((byte[]) value, Charsets.UTF_8, LogParamHelper.MAX_LONG_STRING_SIZE);
        return String.valueOf(value);
    }
}
