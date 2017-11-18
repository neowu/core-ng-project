package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import core.framework.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public abstract class App extends Module {
    private final Logger logger = LoggerFactory.getLogger(App.class);

    public final void start() {
        try {
            configure();
            logger.info("execute startup methods");
            context.startupHook.forEach(java.lang.Runnable::run);
        } catch (Throwable e) {
            logger.error("application failed to start, error={}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public final void configure() {
        logger.info("initialize framework");
        logger.info("availableProcessors={}, maxMemory={}", Threads.availableProcessors(), Runtime.getRuntime().maxMemory());
        context = new ModuleContext(new BeanFactory(), null);
        logger.info("initialize application");
        initialize();
        context.config.validate();
    }
}
