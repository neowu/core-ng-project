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
    static RedisImpl create(ByteArrayOutputStream requestStream, ResponseHolder response) {
        RedisImpl redis = new RedisImpl(null);
        redis.pool = new Pool<>(() -> {
            RedisConnection connection = new RedisConnection(null, Duration.ZERO);
            connection.outputStream = new RedisOutputStream(requestStream, 512);
            connection.inputStream = new RedisInputStream(new ByteArrayInputStream(Strings.bytes(response.data)));
            return connection;
        }, null);
        return redis;
    }

    static class ResponseHolder {
        String data;
    }
}
