package core.framework.module;

import core.framework.async.Task;
import core.framework.internal.asm.ClassPoolFactory;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.validate.Validator;
import core.framework.log.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public abstract class App extends Module {
    private final LogManager logManager = new LogManager();
    private final Logger logger = LoggerFactory.getLogger(App.class);

    public final void start() {
        ActionLog actionLog = logManager.begin("=== startup begin ===");
        boolean failed = false;
        try {
            logContext(actionLog);
            configure();
            logger.info("execute startup tasks");
            for (Task task : context.startupHook) {
                task.execute();
            }
            cleanup();
            logger.info("startup completed, elapsed={}", actionLog.elapsed());
        } catch (Throwable e) {
            logger.error(Markers.errorCode("FAILED_TO_START"), "app failed to start, error={}", e.getMessage(), e);
            failed = true;
        } finally {
            logManager.end("=== startup end ===");
        }
        if (failed) {
            System.exit(1);
        }
    }

    public final void configure() {
        logger.info("initialize framework");
        context = new ModuleContext(logManager);
        Runtime.getRuntime().addShutdownHook(new Thread(context.shutdownHook, "shutdown"));

        logger.info("initialize application");
        initialize();
        context.validate();
    }

    void logContext(ActionLog actionLog) {
        actionLog.action("app:start");
        Runtime runtime = Runtime.getRuntime();
        actionLog.stat("cpu", runtime.availableProcessors());
        actionLog.stat("max_memory", runtime.maxMemory());
    }

    private void cleanup() {
        // free object not used anymore
        Validator.cleanup();
        JSONMapper.cleanup();
        ClassPoolFactory.cleanup();
    }
}
