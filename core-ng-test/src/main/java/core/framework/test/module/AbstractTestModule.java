package core.framework.test.module;

import core.framework.module.InitDBConfig;
import core.framework.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * @author neo
 */
public abstract class AbstractTestModule extends Module {
    private final Logger logger = LoggerFactory.getLogger(AbstractTestModule.class);

    public final void configure() throws Exception {
        logger.info("initialize test context");
        context = new TestModuleContext();
        context.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(context.shutdownHook, "shutdown")); // register shutdown hook for integration test, to clean up external resources if needed

        logger.info("initialize application");
        initialize();
        context.validate();
        context.startupHook.initialize();   // only initialize clients, not start process
    }

    public <T> T overrideBinding(Class<? super T> type, T instance) {
        return overrideBinding(type, null, instance);
    }

    public <T> T overrideBinding(Type type, String name, T instance) {
        return ((TestModuleContext) context).overrideBinding(type, name, instance);
    }

    public InitDBConfig initDB() {
        return initDB(null);
    }

    public InitDBConfig initDB(String name) {
        return context.config(InitDBConfig.class, name);
    }

    public void inject(Object instance) {
        context.beanFactory.inject(instance);
    }
}
