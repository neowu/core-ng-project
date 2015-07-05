package core.framework.api.web.exception;

import core.framework.api.exception.Warning;

/**
 * @author neo
 */
@Warning
public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 5545181864430282120L;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
