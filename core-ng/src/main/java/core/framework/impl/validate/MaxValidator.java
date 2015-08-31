package core.framework.impl.validate;

import core.framework.api.util.Exceptions;
import core.framework.api.validate.Max;

/**
 * @author neo
 */
public class MaxValidator implements FieldValidator {
    private final String fieldPath;
    private final Max max;

    public MaxValidator(String fieldPath, Max max) {
        this.fieldPath = fieldPath;
        this.max = max;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;

        if (value instanceof Number) {
            double numberValue = ((Number) value).doubleValue();
            if (numberValue > max.value()) errors.add(fieldPath, max.message());
        } else {
            throw Exceptions.error("unexpected value type, valueClass={}", value);
        }
    }
}
