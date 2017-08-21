package core.framework.api.web.exception;

import core.framework.api.http.HTTPStatus;
import core.framework.api.log.ErrorCode;
import core.framework.api.log.Severity;
import core.framework.api.web.service.ResponseStatus;

/**
 * @author neo
 */
@ResponseStatus(HTTPStatus.TOO_MANY_REQUESTS)
public final class TooManyRequestsException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 6657854760603154454L;

    public TooManyRequestsException(String message) {
        super(message);
    }

    @Override
    public Severity severity() {
        return Severity.WARN;
    }

    @Override
    public String errorCode() {
        return "TOO_MANY_REQUESTS";
    }
}
