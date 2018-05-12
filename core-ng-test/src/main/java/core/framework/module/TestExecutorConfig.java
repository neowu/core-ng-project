package core.framework.module;

import core.framework.async.Executor;
import core.framework.impl.module.ModuleContext;
import core.framework.test.async.MockExecutor;

/**
 * @author neo
 */
public class TestExecutorConfig extends ExecutorConfig {
    TestExecutorConfig(ModuleContext context) {
        super(context);
    }

    @Override
    Executor createExecutor(String name, int poolSize) {
        return new MockExecutor();
    }
}
