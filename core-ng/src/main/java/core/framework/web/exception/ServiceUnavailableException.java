package core.framework.web.exception;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;

/**
 * @author neo
 */
@ResponseStatus(HTTPStatus.SERVICE_UNAVAILABLE)
public final class ServiceUnavailableException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 3049983486474581542L;

    public ServiceUnavailableException(String message) {
        super(message);
    }

    @Override
    public Severity severity() {
        return Severity.WARN;
    }

    @Override
    public String errorCode() {
        return "SERVICE_UNAVAILABLE";
    }
}
