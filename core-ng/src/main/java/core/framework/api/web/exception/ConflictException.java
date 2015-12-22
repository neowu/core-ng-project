package core.framework.api.web.exception;

import core.framework.api.log.ErrorCode;
import core.framework.api.log.Warning;

/**
 * @author neo
 */
@Warning
public final class ConflictException extends RuntimeException implements ErrorCode {
    public static final String DEFAULT_ERROR_CODE = "CONFLICT";

    private static final long serialVersionUID = 7787085179989898162L;

    private final String errorCode;

    public ConflictException(String message) {
        super(message);
        errorCode = DEFAULT_ERROR_CODE;
    }

    public ConflictException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConflictException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }
}
