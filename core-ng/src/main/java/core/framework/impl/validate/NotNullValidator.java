package core.framework.impl.validate;

/**
 * @author neo
 */
public class NotNullValidator implements FieldValidator {
    private final String fieldPath;
    private final String errorMessage;
    private final boolean enablePartial;

    public NotNullValidator(String fieldPath, String errorMessage, boolean enablePartial) {
        this.fieldPath = fieldPath;
        this.errorMessage = errorMessage;
        this.enablePartial = enablePartial;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (partial && enablePartial) return;
        if (value == null) errors.add(fieldPath, errorMessage);
    }
}
