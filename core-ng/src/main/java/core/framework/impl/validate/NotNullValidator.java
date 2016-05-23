package core.framework.impl.validate;

/**
 * @author neo
 */
class NotNullValidator implements FieldValidator {
    private final String fieldPath;
    private final String errorMessage;

    NotNullValidator(String fieldPath, String errorMessage) {
        this.fieldPath = fieldPath;
        this.errorMessage = errorMessage;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (partial) return;
        if (value == null) errors.add(fieldPath, errorMessage);
    }
}
