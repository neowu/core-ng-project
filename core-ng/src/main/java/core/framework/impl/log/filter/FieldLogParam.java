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
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        if (maskedFields.contains(field)) {
            builder.append("******");
        } else {
            builder.append(value);
        }
    }
}
