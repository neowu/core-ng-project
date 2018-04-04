package core.framework.impl.log.filter;

import java.util.Arrays;
import java.util.Set;

/**
 * @author neo
 */
public class FieldParam implements FilterParam {
    private final Object field;
    private final Object value;

    public FieldParam(Object field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public String filter(Set<String> maskedFields) {
        if (maskedFields.contains(String.valueOf(field))) return "******";
        if (value instanceof Object[]) return Arrays.toString((Object[]) value);
        return String.valueOf(value);
    }
}
