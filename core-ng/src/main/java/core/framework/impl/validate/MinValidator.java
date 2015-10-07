package core.framework.impl.validate;

import core.framework.api.validate.Min;

/**
 * @author neo
 */
public class MinValidator implements FieldValidator {
    private final String fieldPath;
    private final Min min;

    public MinValidator(String fieldPath, Min min) {
        this.fieldPath = fieldPath;
        this.min = min;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;

        double numberValue = ((Number) value).doubleValue();
        if (numberValue < min.value()) errors.add(fieldPath, min.message());
    }
}
