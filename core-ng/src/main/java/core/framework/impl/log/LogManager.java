package core.framework.impl.log;

import core.framework.api.log.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class LogManager {
    private final ThreadLocal<ActionLogger> loggers = new ThreadLocal<>();
    public final LogWriter logWriter = new LogWriter();
    public LogForwarder logForwarder;
    public final String appName;

    public LogManager() {
        this.appName = System.getProperty("core.appName");
    }

    public void start(Logger logger, String message) {
        ActionLogger actionLogger = new ActionLogger(logWriter, logForwarder);
        loggers.set(actionLogger);
        logger.debug(message);
        actionLogger.log.logId();
    }

    public void end(Logger logger, String message) {
        logger.debug(message);
        loggers.get().end();
        loggers.remove();
    }

    public void process(LogEvent event) {
        ActionLogger logger = loggers.get();
        if (logger != null) logger.process(event);
    }

    public ActionLog currentActionLog() {
        ActionLogger logger = loggers.get();
        if (logger == null) return null;
        return logger.log;
    }

    public void shutdown() {
        Logger logger = LoggerFactory.getLogger(LogManager.class.getName());
        logger.info("showdown log manager");
        logWriter.close();
        if (logForwarder != null) logForwarder.shutdown();
    }

    public void logError(Logger logger, Throwable e) {  // pass logger where the exception is caught
        ActionLog actionLog = currentActionLog();
        actionLog.error(e);

        String errorMessage = e.getMessage();
        if (e.getClass().isAnnotationPresent(Warning.class)) {
            logger.warn(errorMessage, e);
        } else {
            logger.error(errorMessage, e);
        }
    }
}
