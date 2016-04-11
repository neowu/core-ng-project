package core.framework.api.http;

import core.framework.api.log.ErrorCode;

/**
 * @author neo
 */
public class HTTPClientException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = -7577571026619549539L;

    private final String errorCode;

    public HTTPClientException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }
}
