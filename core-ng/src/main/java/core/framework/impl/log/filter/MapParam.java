package core.framework.impl.log.filter;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class MapParam implements FilterParam {
    private final Map<String, String> values;

    public MapParam(Map<String, String> values) {
        this.values = values;
    }

    @Override
    public String filter(Set<String> maskedFields) {
        var builder = new StringBuilder();
        builder.append('{');
        int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (index > 0) builder.append(", ");
            String key = entry.getKey();
            builder.append(key).append('=');

            if (maskedFields.contains(key)) builder.append("******");
            else builder.append(entry.getValue());

            index++;
        }
        return builder.append('}').toString();
    }
}
