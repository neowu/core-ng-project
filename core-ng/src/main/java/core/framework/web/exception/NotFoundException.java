package core.framework.web.exception;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;

/**
 * @author neo
 */
@ResponseStatus(HTTPStatus.NOT_FOUND)
public final class NotFoundException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 8663360723004690205L;

    private final String errorCode;

    public NotFoundException(String message) {
        super(message);
        errorCode = "NOT_FOUND";
    }

    public NotFoundException(String message, String errorCode) {
        super(message);
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
