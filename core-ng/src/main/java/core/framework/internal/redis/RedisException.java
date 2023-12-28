package core.framework.internal.redis;

import java.io.Serial;

/**
 * @author neo
 */
public class RedisException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5459336238321986524L;

    public RedisException(String message) {
        super(message);
    }
}
