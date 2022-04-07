package core.framework.http;

import core.framework.log.ErrorCode;

import java.io.Serial;

/**
 * @author neo
 */
public final class HTTPClientException extends RuntimeException implements ErrorCode {
    @Serial
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
