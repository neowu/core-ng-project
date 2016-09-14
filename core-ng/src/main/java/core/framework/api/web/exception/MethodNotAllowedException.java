package core.framework.api.web.exception;

import core.framework.api.log.ErrorCode;
import core.framework.api.log.Severity;

/**
 * @author neo
 */
public final class MethodNotAllowedException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 2349080664326196294L;

    public MethodNotAllowedException(String message) {
        super(message);
    }

    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Severity severity() {
        return Severity.WARN;
    }

    @Override
    public String errorCode() {
        return "METHOD_NOT_ALLOWED";
    }
}
