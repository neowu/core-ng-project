package core.framework.impl.validate;

import core.framework.api.validate.NotNull;

/**
 * @author neo
 */
public class NotNullValidator implements FieldValidator {
    private final String fieldPath;
    private final NotNull notNull;

    public NotNullValidator(String fieldPath, NotNull notNull) {
        this.fieldPath = fieldPath;
        this.notNull = notNull;
    }

    @Override
    public void validate(Object value, ValidationResult result) {
        if (value == null) result.addError(fieldPath, notNull.message());
    }
}
