package core.framework.impl.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RedisImplTest {
    private RedisImpl redis;

    @BeforeEach
    void createRedis() {
        redis = new RedisImpl(null);
    }

    @Test
    void close() {
        redis.close();
    }

    @Test
    void timeout() {
        Duration timeout = Duration.ofSeconds(5);
        redis.timeout(timeout);

        assertEquals(timeout, redis.timeout);
    }

    @Test
    void slowOperationThreshold() {
        Duration threshold = Duration.ofSeconds(5);
        redis.slowOperationThreshold(threshold);

        assertEquals(threshold.toNanos(), redis.slowOperationThresholdInNanos);
    }
}
