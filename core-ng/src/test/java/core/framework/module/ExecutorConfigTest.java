package core.framework.module;

import core.framework.async.Executor;
import core.framework.internal.async.ThreadPools;
import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ExecutorConfigTest {
    private ExecutorConfig config;

    @BeforeEach
    void createExecutorConfig() {
        config = new ExecutorConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void createExecutor() {
        Executor executor = config.createExecutor(ThreadPools.virtualThreadExecutor("test-"));
        assertThat(executor).isNotNull();
    }
}
