package core.framework.api.module;

import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author neo
 */
public class LogConfig {
    private final Logger logger = LoggerFactory.getLogger(LogConfig.class);

    private final ModuleContext context;

    public LogConfig(ModuleContext context) {
        this.context = context;
    }

    public void actionLogPath(Path actionLogPath) {
        if (context.test) {
            logger.info("use console output for action log during test");
        } else {
            context.logManager.logWriter.actionLogPath(actionLogPath);
        }
    }

    public void traceLogPath(Path traceLogPath) {
        if (context.test) {
            logger.info("use console output for trace log during test");
        } else {
            context.logManager.logWriter.traceLogPath(traceLogPath);
        }
    }
}
