package core.framework.api.web.exception;

/**
 * @author neo
 */
public class ForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 5472429043879214361L;

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
