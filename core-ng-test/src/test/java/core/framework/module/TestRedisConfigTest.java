package core.framework.module;

import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.TestModuleContext;
import core.framework.test.redis.MockRedis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestRedisConfigTest {
    @Test
    void initialize() {
        TestRedisConfig config = new TestRedisConfig(new TestModuleContext(new TestBeanFactory()));
        assertThat(config.redis).isInstanceOf(MockRedis.class);
    }
}
