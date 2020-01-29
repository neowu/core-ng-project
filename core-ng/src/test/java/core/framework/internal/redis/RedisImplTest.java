package core.framework.internal.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

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

    @Test
    void parseInfo() {
        String info = "# Server\r\nredis_version:5.0.7\r\nconfig_file:\r\n\r\n# Clients\r\nconnected_clients:1\r\n";
        Map<String, String> values = redis.parseInfo(info);

        assertThat(values)
                .containsEntry("redis_version", "5.0.7")
                .containsEntry("config_file", "")
                .containsEntry("connected_clients", "1");
    }
}
