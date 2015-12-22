package core.framework.api.web.exception;

import core.framework.api.log.ErrorCode;
import core.framework.api.log.Warning;

/**
 * @author neo
 */
@Warning
public final class NotFoundException extends RuntimeException implements ErrorCode {
    private static final String DEFAULT_ERROR_CODE = "NOT_FOUND";
    private static final long serialVersionUID = 8663360723004690205L;

    private final String errorCode;

    public NotFoundException(String message) {
        super(message);
        errorCode = DEFAULT_ERROR_CODE;
    }

    public NotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public NotFoundException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }
}
