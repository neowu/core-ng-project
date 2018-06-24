package core.framework.module;

import core.framework.async.Task;
import core.framework.impl.module.ModuleContext;
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
            for (Task task : context.startupHook) {
                task.execute();
            }
        } catch (Throwable e) {
            logger.error("application failed to start, error={}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public final void configure() {
        logger.info("initialize framework");
        Runtime runtime = Runtime.getRuntime();
        logger.info("availableProcessors={}, maxMemory={}", runtime.availableProcessors(), runtime.maxMemory());
        context = new ModuleContext();
        logger.info("initialize application");
        initialize();
        context.validate();
    }
}
