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
        Executor executor;
        if (!context.isTest()) {
            String prefix = "executor-" + (name == null ? "" : name + "-");
            executor = new ExecutorImpl(ThreadPools.cachedThreadPool(poolSize, prefix), context.logManager, name);
            context.shutdownHook.add(((ExecutorImpl) executor)::stop);
        } else {
            executor = context.mockFactory.create(Executor.class);
        }
        context.beanFactory.bind(Executor.class, name, executor);
        return executor;
    }
}
