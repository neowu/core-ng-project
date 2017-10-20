package core.framework.redis.impl;

/**
 * @author neo
 */
public class RedisException extends RuntimeException {
    private static final long serialVersionUID = -5459336238321986524L;

    public RedisException(String message) {
        super(message);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisException(Throwable cause) {
        super(cause);
    }
}
