package core.framework.test.module;

import core.framework.module.APIConfig;
import core.framework.module.CacheConfig;
import core.framework.module.DBConfig;
import core.framework.module.ExecutorConfig;
import core.framework.module.KafkaConfig;
import core.framework.module.LogConfig;
import core.framework.module.RedisConfig;
import core.framework.module.SessionConfig;
import core.framework.module.TestAPIConfig;
import core.framework.module.TestCacheConfig;
import core.framework.module.TestDBConfig;
import core.framework.module.TestExecutorConfig;
import core.framework.module.TestKafkaConfig;
import core.framework.module.TestLogConfig;
import core.framework.module.TestRedisConfig;
import core.framework.module.TestSessionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestModuleContextTest {
    private TestModuleContext context;

    @BeforeEach
    void createTestModuleContext() {
        context = new TestModuleContext();
    }

    @Test
    void targetConfigClass() {
        assertThat(context.configClass(APIConfig.class)).isEqualTo(TestAPIConfig.class);
        assertThat(context.configClass(CacheConfig.class)).isEqualTo(TestCacheConfig.class);
        assertThat(context.configClass(DBConfig.class)).isEqualTo(TestDBConfig.class);
        assertThat(context.configClass(ExecutorConfig.class)).isEqualTo(TestExecutorConfig.class);
        assertThat(context.configClass(KafkaConfig.class)).isEqualTo(TestKafkaConfig.class);
        assertThat(context.configClass(LogConfig.class)).isEqualTo(TestLogConfig.class);
        assertThat(context.configClass(RedisConfig.class)).isEqualTo(TestRedisConfig.class);
        assertThat(context.configClass(SessionConfig.class)).isEqualTo(TestSessionConfig.class);
    }
}
