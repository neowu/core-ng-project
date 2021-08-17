package core.framework.internal.redis;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisAdminOperationTest extends AbstractRedisOperationTest {
    @Test
    void info() {
        response("$31\r\n# Server\r\nredis_version:5.0.7\r\n\r\n");
        Map<String, String> info = redis.admin().info();

        assertThat(info).containsEntry("redis_version", "5.0.7");
        assertRequestEquals("*1\r\n$4\r\nINFO\r\n");
    }
}
