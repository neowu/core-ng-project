package core.framework.api;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public abstract class AbstractApplication extends Module {
    private final Logger logger = LoggerFactory.getLogger(AbstractApplication.class);

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
        context = new ModuleContext(new BeanFactory(), false);

        logger.info("initialize application");
        initialize();
    }
}
