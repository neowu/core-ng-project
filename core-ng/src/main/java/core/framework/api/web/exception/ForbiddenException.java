package core.framework.api.web.exception;

import core.framework.api.log.ErrorCode;

/**
 * @author neo
 */
public final class ForbiddenException extends RuntimeException implements ErrorCode {
    public static final String DEFAULT_ERROR_CODE = "FORBIDDEN";

    private static final long serialVersionUID = 5472429043879214361L;

    private final String errorCode;

    public ForbiddenException(String message) {
        super(message);
        errorCode = DEFAULT_ERROR_CODE;
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
    public String errorCode() {
        return errorCode;
    }
}
