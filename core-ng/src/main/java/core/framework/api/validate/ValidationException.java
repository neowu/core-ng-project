package core.framework.api.validate;

import core.framework.api.log.ErrorCode;
import core.framework.api.log.Severity;
import core.framework.api.util.Strings;

import java.util.Map;

/**
 * @author neo
 */
public class ValidationException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 9215299700445046388L;
    public final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(Strings.format("validation failed, error={}", errors));
        this.errors = errors;
    }

    @Override
    public Severity severity() {
        return Severity.WARN;
    }

    @Override
    public String errorCode() {
        return "VALIDATION_ERROR";
    }
}
