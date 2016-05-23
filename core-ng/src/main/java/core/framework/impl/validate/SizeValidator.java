package core.framework.impl.validate;

import core.framework.api.util.Exceptions;
import core.framework.api.validate.Size;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class SizeValidator implements FieldValidator {
    private final String fieldPath;
    private final Size size;

    SizeValidator(String fieldPath, Size size) {
        this.fieldPath = fieldPath;
        this.size = size;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;

        int size;
        if (value instanceof List) {
            size = ((List) value).size();
        } else if (value instanceof Map) {
            size = ((Map) value).size();
        } else {
            throw Exceptions.error("unexpected value type, valueClass={}", value.getClass().getCanonicalName());
        }

        if (this.size.min() > -1 && size < this.size.min()
            || this.size.max() > -1 && size > this.size.max()) {
            errors.add(fieldPath, this.size.message());
        }
    }
}
