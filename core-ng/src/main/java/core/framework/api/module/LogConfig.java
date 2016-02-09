package core.framework.api.module;

import core.framework.impl.log.ActionLogger;
import core.framework.impl.log.LogForwarder;
import core.framework.impl.log.TraceLogger;
import core.framework.impl.log.stat.CollectStatJob;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.FixedRateTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;

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
        if (context.isTest()) {
            logger.info("disable action log during test");
        } else {
            context.logManager.actionLogger = ActionLogger.console();
        }
    }

    public void writeActionLogToFile(Path actionLogPath) {
        if (context.isTest()) {
            logger.info("disable action log during test");
        } else {
            context.logManager.actionLogger = ActionLogger.file(actionLogPath);
        }
    }

    public void writeTraceLogToConsole() {
        if (context.isTest()) {
            logger.info("disable trace log during test");
        } else {
            context.logManager.traceLogger = TraceLogger.console();
        }
    }

    public void writeTraceLogToFile(Path traceLogPath) {
        if (context.isTest()) {
            logger.info("disable trace log during test");
        } else {
            context.logManager.traceLogger = TraceLogger.file(traceLogPath);
        }
    }

    public void forwardLogToRemote(String host) {
        if (context.isTest()) {
            logger.info("disable log forwarding during test");
        } else {
            context.logManager.logForwarder = new LogForwarder(host, context.logManager.appName);
            context.scheduler().addTrigger(new FixedRateTrigger("collect-stat", new CollectStatJob(context.logManager.logForwarder), Duration.ofSeconds(10)));
        }
    }
}
