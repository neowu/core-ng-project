package core.framework.api.web.exception;

import core.framework.api.http.HTTPStatus;

/**
 * @author neo
 */
public class RemoteServiceException extends RuntimeException {
    private static final long serialVersionUID = 6935063785656278927L;

    public final HTTPStatus status;
    public String id;
    public String errorCode;

    public RemoteServiceException(String message, HTTPStatus status) {
        super(message);
        this.status = status;
    }

    public RemoteServiceException(String message, HTTPStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
