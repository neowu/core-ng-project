package core.framework.module;

import core.framework.redis.Redis;
import core.framework.test.redis.MockRedis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestRedisConfigTest {
    private TestRedisConfig config;

    @BeforeEach
    void createTestRedisConfig() {
        config = new TestRedisConfig();
    }

    @Test
    void createRedis() {
        Redis redis = config.createRedis();
        assertThat(redis).isInstanceOf(MockRedis.class);
    }
}
