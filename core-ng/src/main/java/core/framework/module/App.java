package core.framework.module;

import core.framework.async.Task;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import core.framework.log.Markers;
import core.framework.util.Network;
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
            logger.info("execute startup methods");
            for (Task task : context.startupHook) {
                task.execute();
            }
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

    void logContext(ActionLog actionLog) {
        actionLog.action("app:start");
        Runtime runtime = Runtime.getRuntime();
        actionLog.stat("cpu", runtime.availableProcessors());
        actionLog.stat("max_memory", runtime.maxMemory());
        actionLog.context("host", Network.LOCAL_HOST_NAME);
    }

    public final void configure() {
        logger.info("initialize framework");
        context = new ModuleContext(logManager);

        logger.info("initialize application");
        initialize();
        context.validate();
    }
}
