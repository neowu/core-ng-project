package core.framework.module;

import core.framework.async.Executor;
import core.framework.impl.async.ExecutorImpl;
import core.framework.impl.async.ThreadPools;
import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public class ExecutorConfig {
    private final ModuleContext context;

    ExecutorConfig(ModuleContext context) {
        this.context = context;
    }

    public Executor add() {
        return add(null, Runtime.getRuntime().availableProcessors() * 2);
    }

    public Executor add(String name, int poolSize) {
        Executor executor = createExecutor(name, poolSize);
        context.beanFactory.bind(Executor.class, name, executor);
        return executor;
    }

    Executor createExecutor(String name, int poolSize) {
        String prefix = "executor-" + (name == null ? "" : name + "-");
        Executor executor = new ExecutorImpl(ThreadPools.cachedThreadPool(poolSize, prefix), context.logManager, name);
        context.shutdownHook.add(((ExecutorImpl) executor)::stop);
        return executor;
    }
}
