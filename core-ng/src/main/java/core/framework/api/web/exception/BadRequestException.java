package core.framework.api.web.exception;

import core.framework.api.log.Warning;

/**
 * @author neo
 */
@Warning
public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = -2304226404736886782L;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }
}
