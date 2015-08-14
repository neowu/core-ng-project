package core.framework.impl.log;

import core.framework.api.log.Warning;
import org.slf4j.Logger;

/**
 * @author neo
 */
public class LogManager {
    private final ThreadLocal<ActionLogger> loggers = new ThreadLocal<>();
    Logger logger;
    public final LogWriter logWriter = new LogWriter();
    public LogForwarder logForwarder;
    public final String appName;

    public LogManager() {
        this.appName = System.getProperty("core.appName");
    }

    public void start(String message) {
        ActionLogger actionLogger = new ActionLogger(logWriter, logForwarder);
        loggers.set(actionLogger);
        logger.debug(message);
        logger.debug("[context] id={}", actionLogger.log.id);
    }

    public void end(String message) {
        logger.debug(message);
        ActionLogger actionLogger = loggers.get();
        loggers.remove();   // remove action logger first, make all log during end() not appending to trace
        actionLogger.end();
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
        logger.info("showdown log manager");
        if (logForwarder != null) logForwarder.shutdown();
        logWriter.close();
    }

    public void logError(Throwable e) {  // pass logger where the exception is caught
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
