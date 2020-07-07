package core.framework.test.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MockRedisAdminTest {
    private MockRedis redis;

    @BeforeEach
    void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    void info() {
        assertThat(redis.admin().info()).isNotNull();
    }
}
