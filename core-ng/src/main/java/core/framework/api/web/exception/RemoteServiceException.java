package core.framework.api.web.exception;

import core.framework.api.http.HTTPStatus;

/**
 * @author neo
 */
public final class RemoteServiceException extends RuntimeException {
    private static final long serialVersionUID = 6935063785656278927L;

    public final HTTPStatus status;
    public final String errorCode;

    public String id;

    public RemoteServiceException(String message, HTTPStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public RemoteServiceException(String message, HTTPStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
