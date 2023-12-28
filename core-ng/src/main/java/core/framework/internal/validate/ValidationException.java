package core.framework.internal.validate;

import core.framework.log.ErrorCode;

import java.io.Serial;
import java.util.Map;

/**
 * @author neo
 */
public final class ValidationException extends RuntimeException implements ErrorCode {
    @Serial
    private static final long serialVersionUID = -2784801858422440222L;
    public final transient Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("validation failed, error=" + errors);
        this.errors = errors;
    }

    @Override
    public String errorCode() {
        return "VALIDATION_ERROR";
    }
}
