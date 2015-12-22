package core.framework.api.web.exception;

import core.framework.api.log.ErrorCode;
import core.framework.api.log.Warning;

/**
 * @author neo
 */
@Warning
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
    public String errorCode() {
        return errorCode;
    }
}
