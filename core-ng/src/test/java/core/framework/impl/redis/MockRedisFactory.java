package core.framework.impl.redis;

import core.framework.impl.resource.Pool;
import core.framework.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;

/**
 * @author neo
 */
class MockRedisFactory {
    static RedisImpl create(ByteArrayOutputStream requestStream, String response) {
        RedisImpl redis = new RedisImpl(null);
        redis.pool = new Pool<>(() -> {
            RedisConnection connection = new RedisConnection(null, Duration.ZERO);
            connection.outputStream = new RedisOutputStream(requestStream);
            connection.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes(response)));
            return connection;
        }, null);
        return redis;
    }
}
