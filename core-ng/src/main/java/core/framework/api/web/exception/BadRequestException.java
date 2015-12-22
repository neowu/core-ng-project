package core.framework.api.web.exception;

import core.framework.api.log.ErrorCode;
import core.framework.api.log.Warning;

/**
 * @author neo
 */
@Warning
public final class BadRequestException extends RuntimeException implements ErrorCode {
    public static final String DEFAULT_ERROR_CODE = "BAD_REQUEST";

    private static final long serialVersionUID = -2304226404736886782L;

    private final String errorCode;

    public BadRequestException(String message) {
        super(message);
        errorCode = DEFAULT_ERROR_CODE;
    }

    public BadRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BadRequestException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }
}
