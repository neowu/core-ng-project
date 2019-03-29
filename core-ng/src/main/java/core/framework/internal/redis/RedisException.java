package core.framework.internal.redis;

/**
 * @author neo
 */
public class RedisException extends RuntimeException {
    private static final long serialVersionUID = -5459336238321986524L;

    RedisException(String message) {
        super(message);
    }
}
