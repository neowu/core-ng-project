package core.framework.impl.validate;

import core.framework.log.ErrorCode;
import core.framework.util.Strings;

import java.util.Map;

/**
 * @author neo
 */
public final class ValidationException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = -2784801858422440222L;
    public final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(Strings.format("validation failed, error={}", errors));
        this.errors = errors;
    }

    @Override
    public String errorCode() {
        return "VALIDATION_ERROR";
    }
}
