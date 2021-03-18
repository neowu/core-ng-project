package core.framework.web.exception;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;

import java.io.Serial;

/**
 * @author neo
 */
@ResponseStatus(HTTPStatus.FORBIDDEN)
public final class ForbiddenException extends RuntimeException implements ErrorCode {
    @Serial
    private static final long serialVersionUID = 5472429043879214361L;

    private final String errorCode;

    public ForbiddenException(String message) {
        super(message);
        errorCode = "FORBIDDEN";
    }

    public ForbiddenException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ForbiddenException(String message, String errorCode, Throwable cause) {
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
