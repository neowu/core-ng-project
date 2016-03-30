package core.framework.impl.validate;

import java.util.regex.Pattern;

/**
 * @author neo
 */
class PatternValidator implements FieldValidator {
    private final String fieldPath;
    private final String errorMessage;
    private final Pattern pattern;

    PatternValidator(String pattern, String fieldPath, String errorMessage) {
        this.pattern = Pattern.compile(pattern);
        this.fieldPath = fieldPath;
        this.errorMessage = errorMessage;
    }

    @Override
    public void validate(Object value, ValidationErrors errors, boolean partial) {
        if (value == null) return;

        if (!pattern.matcher((String) value).matches()) errors.add(fieldPath, errorMessage);
    }
}
