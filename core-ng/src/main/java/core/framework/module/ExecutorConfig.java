package core.framework.module;

import core.framework.async.Executor;
import core.framework.impl.async.ExecutorImpl;
import core.framework.impl.async.ThreadPools;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;

/**
 * @author neo
 */
public class ExecutorConfig extends Config {
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
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
        var executor = new ExecutorImpl(ThreadPools.cachedThreadPool(poolSize, prefix), context.logManager, name);
        context.shutdownHook.add(ShutdownHook.STAGE_2, executor::shutdown);
        return executor;
    }
}
