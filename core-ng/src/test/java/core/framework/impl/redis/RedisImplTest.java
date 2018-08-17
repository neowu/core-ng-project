package core.framework.impl.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

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
        var timeout = Duration.ofSeconds(5);
        redis.timeout(timeout);

        assertThat(redis.timeoutInMs).isEqualTo(timeout.toMillis());
    }

    @Test
    void slowOperationThreshold() {
        var threshold = Duration.ofSeconds(5);
        redis.slowOperationThreshold(threshold);

        assertThat(redis.slowOperationThresholdInNanos).isEqualTo(threshold.toNanos());
    }
}
