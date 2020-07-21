package core.framework.module;

import core.framework.async.Executor;
import core.framework.internal.async.ExecutorImpl;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;

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
        var executor = new ExecutorImpl(poolSize, name, context.logManager);
        context.shutdownHook.add(ShutdownHook.STAGE_2, timeout -> executor.shutdown());
        context.shutdownHook.add(ShutdownHook.STAGE_3, executor::awaitTermination);
        return executor;
    }
}
