package core.framework.module;

import core.framework.async.Executor;
import core.framework.test.async.MockExecutor;

import java.util.concurrent.ExecutorService;

/**
 * @author neo
 */
public class TestExecutorConfig extends ExecutorConfig {
    @Override
    Executor createExecutor(ExecutorService threadExecutor) {
        return new MockExecutor();
    }
}
