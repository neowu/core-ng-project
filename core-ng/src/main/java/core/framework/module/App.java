package core.framework.module;

import com.sun.management.OperatingSystemMXBean;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.json.JSONMapper;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.validate.Validator;
import core.framework.log.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 * @author neo
 */
public abstract class App extends Module {
    private final LogManager logManager = new LogManager();
    private final Logger logger = LoggerFactory.getLogger(App.class);

    public final void start() {
        ActionLog actionLog = logManager.begin("=== startup begin ===", null);
        boolean failed = false;
        try {
            logContext(actionLog);
            configure();
            context.probe.check();    // readiness probe only needs to run on actual startup, not on test
            logger.info("execute startup tasks");
            context.startupHook.initialize();
            context.startupHook.start();
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

    void logContext(ActionLog actionLog) {
        actionLog.action("app:start");
    }

    public final void configure() {
        logger.info("initialize framework");
        Runtime runtime = Runtime.getRuntime();
        logger.info("availableProcessors={}, maxMemory={}", runtime.availableProcessors(), ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize());
        logger.info("jvmArgs={}", String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));

        context = new ModuleContext(logManager);
        context.initialize();
        runtime.addShutdownHook(new Thread(context.shutdownHook, "shutdown"));

        logger.info("initialize application");
        initialize();
        context.validate();
    }

    private void cleanup() {
        // free static objects not used anymore
        Validator.cleanup();
        JSONMapper.cleanup();
        if (!context.httpServer.siteManager.webDirectory.localEnv) {    // for local env, it may rebuild html template at runtime
            DynamicInstanceBuilder.cleanup();
        }
    }
}
