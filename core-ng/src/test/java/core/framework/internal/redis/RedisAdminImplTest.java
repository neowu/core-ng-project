package core.framework.internal.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisAdminImplTest {
    private RedisAdminImpl redis;

    @BeforeEach
    void createRedisAdminImpl() {
        redis = new RedisAdminImpl(null);
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
