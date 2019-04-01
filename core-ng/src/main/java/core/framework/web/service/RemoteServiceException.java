package core.framework.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;

/**
 * @author neo
 */
public final class RemoteServiceException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 6935063785656278927L;

    public final HTTPStatus status;
    private final Severity severity;
    private final String errorCode;

    public RemoteServiceException(String message, Severity severity, String errorCode, HTTPStatus status) {
        super(message);
        this.severity = severity;
        this.errorCode = errorCode;
        this.status = status;
    }

    public RemoteServiceException(String message, Severity severity, String errorCode, HTTPStatus status, Throwable cause) {
        super(message, cause);
        this.severity = severity;
        this.errorCode = errorCode;
        this.status = status;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }

    @Override
    public Severity severity() {
        return severity;
    }
}
