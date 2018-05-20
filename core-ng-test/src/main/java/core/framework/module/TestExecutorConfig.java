package core.framework.module;

import core.framework.async.Executor;
import core.framework.test.async.MockExecutor;

/**
 * @author neo
 */
public class TestExecutorConfig extends ExecutorConfig {
    @Override
    Executor createExecutor(String name, int poolSize) {
        return new MockExecutor();
    }
}
