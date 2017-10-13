package core.framework.validate;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import core.framework.util.Strings;

import java.util.Map;

/**
 * @author neo
 */
@ResponseStatus(HTTPStatus.BAD_REQUEST)
public final class ValidationException extends RuntimeException implements ErrorCode {
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
