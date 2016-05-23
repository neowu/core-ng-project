package core.framework.impl.validate;

import core.framework.api.validate.Length;

/**
 * @author neo
 */
class LengthValidator implements FieldValidator {
    private final String fieldPath;
    private final Length length;

    LengthValidator(String fieldPath, Length length) {
        this.fieldPath = fieldPath;
        this.length = length;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;

        int length = ((String) value).length();

        if (this.length.min() > -1 && length < this.length.min()
            || this.length.max() > -1 && length > this.length.max()) {
            errors.add(fieldPath, this.length.message());
        }
    }
}
