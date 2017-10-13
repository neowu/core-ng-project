package core.framework.web.exception;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;

/**
 * @author neo
 */
@ResponseStatus(HTTPStatus.UNAUTHORIZED)
public final class UnauthorizedException extends RuntimeException implements ErrorCode {
    public static final String DEFAULT_ERROR_CODE = "UNAUTHORIZED";

    private static final long serialVersionUID = 5545181864430282120L;

    private final String errorCode;

    public UnauthorizedException(String message) {
        super(message);
        errorCode = DEFAULT_ERROR_CODE;
    }

    public UnauthorizedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnauthorizedException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public Severity severity() {
        return Severity.WARN;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }
}
