package core.framework.impl.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public class FieldLogParam implements LogParam {
    private final String field;
    private final String value;

    public FieldLogParam(String field, String value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public String filter(Set<String> maskedFields) {
        if (maskedFields.contains(field)) return "******";
        return value;
    }
}
