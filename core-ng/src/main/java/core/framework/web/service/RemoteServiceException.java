package core.framework.web.service;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;

/**
 * @author neo
 */
public final class RemoteServiceException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 6935063785656278927L;

    private final Severity severity;
    private final String errorCode;

    public RemoteServiceException(String message, Severity severity, String errorCode) {
        super(message);
        this.severity = severity;
        this.errorCode = errorCode;
    }

    public RemoteServiceException(String message, Severity severity, String errorCode, Throwable cause) {
        super(message, cause);
        this.severity = severity;
        this.errorCode = errorCode;
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
