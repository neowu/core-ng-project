package core.framework.api.web.exception;

/**
 * @author neo
 */
public class RemoteServiceException extends RuntimeException {
    private static final long serialVersionUID = 6935063785656278927L;

    public RemoteServiceException(String message) {
        super(message);
    }

    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
