package core.framework.impl.resource;

import core.framework.api.log.ErrorCode;

/**
 * @author neo
 */
public final class PoolException extends RuntimeException implements ErrorCode {
    private static final long serialVersionUID = 2151045905336463247L;

    private final String errorCode;

    public PoolException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public String errorCode() {
        return errorCode;
    }
}
