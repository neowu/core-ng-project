package core.framework.api.module;

import core.framework.impl.log.ActionLogWriter;
import core.framework.impl.log.LogForwarder;
import core.framework.impl.log.TraceLogWriter;
import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author neo
 */
public final class LogConfig {
    private final Logger logger = LoggerFactory.getLogger(LogConfig.class);

    private final ModuleContext context;

    public LogConfig(ModuleContext context) {
        this.context = context;
    }

    public void writeActionLogToConsole() {
        if (context.test) {
            logger.info("disable action log during test");
        } else {
            context.logManager.actionLogWriter = ActionLogWriter.console();
        }
    }

    public void writeActionLogToFile(Path actionLogPath) {
        if (context.test) {
            logger.info("disable action log during test");
        } else {
            context.logManager.actionLogWriter = ActionLogWriter.file(actionLogPath);
        }
    }

    public void writeTraceLogToConsole() {
        if (context.test) {
            logger.info("disable trace log during test");
        } else {
            context.logManager.traceLogWriter = TraceLogWriter.console();
        }
    }

    public void writeTraceLogToFile(Path traceLogPath) {
        if (context.test) {
            logger.info("disable trace log during test");
        } else {
            context.logManager.traceLogWriter = TraceLogWriter.file(traceLogPath);
        }
    }

    public void forwardLogToRemote(String host) {
        if (context.test) {
            logger.info("disable log forwarding during test");
        } else {
            context.logManager.logForwarder = new LogForwarder(host, context.logManager.appName);
            context.startupHook.add(context.logManager.logForwarder::start);
        }
    }
}
