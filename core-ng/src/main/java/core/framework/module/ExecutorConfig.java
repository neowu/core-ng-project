package core.framework.module;

import core.framework.async.Executor;
import core.framework.internal.async.ExecutorImpl;
import core.framework.internal.async.ThreadPools;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author neo
 */
public class ExecutorConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    public Executor add() {
        logger.info("create virtual thread executor");
        Executor executor = createExecutor(ThreadPools.virtualThreadExecutor("executor-"));
        context.beanFactory.bind(Executor.class, null, executor);
        return executor;
    }

    public Executor add(String name, int poolSize) {
        logger.info("create executor, name={}, poolSize={}", name, poolSize);
        Executor executor = createExecutor(ThreadPools.cachedThreadPool(poolSize, "executor" + (name == null ? "" : "-" + name) + "-"));
        context.beanFactory.bind(Executor.class, name, executor);
        return executor;
    }

    Executor createExecutor(ExecutorService threadExecutor) {
        var executor = new ExecutorImpl(threadExecutor, context.logManager, context.shutdownHook.shutdownTimeoutInNano);
        context.shutdownHook.add(ShutdownHook.STAGE_2, timeout -> executor.shutdown());
        context.shutdownHook.add(ShutdownHook.STAGE_3, executor::awaitTermination);
        return executor;
    }
}
