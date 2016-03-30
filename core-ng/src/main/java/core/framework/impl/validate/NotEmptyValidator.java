package core.framework.impl.validate;

import core.framework.api.util.Strings;

/**
 * @author neo
 */
class NotEmptyValidator implements FieldValidator {
    private final String fieldPath;
    private final String errorMessage;

    NotEmptyValidator(String fieldPath, String errorMessage) {
        this.fieldPath = fieldPath;
        this.errorMessage = errorMessage;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;

        if (Strings.isEmpty((String) value)) errors.add(fieldPath, errorMessage);
    }
}
