package core.framework.impl.log;

import core.framework.api.log.Warning;
import org.slf4j.Logger;

/**
 * @author neo
 */
public class LogManager {
    private final ThreadLocal<ActionLogger> actionLoggers = new ThreadLocal<>();
    Logger logger;
    public ActionLogWriter actionLogWriter;
    public TraceLogWriter traceLogWriter;
    public LogForwarder logForwarder;
    public final String appName;

    public LogManager() {
        this.appName = System.getProperty("core.appName");
    }

    public void begin(String message) {
        if (actionLogWriter == null && traceLogWriter == null && logForwarder == null) return;

        ActionLogger actionLogger = new ActionLogger(actionLogWriter, traceLogWriter, logForwarder);
        actionLoggers.set(actionLogger);
        logger.debug(message);
        logger.debug("[context] id={}", actionLogger.log.id);
    }

    public void end(String message) {
        logger.debug(message);
        ActionLogger actionLogger = actionLoggers.get();
        actionLoggers.remove();   // remove action logger first, make all log during end() not appending to trace
        actionLogger.end();
    }

    public void process(LogEvent event) {
        ActionLogger logger = actionLoggers.get();
        if (logger != null) logger.process(event);
    }

    public void stop() {
        if (logForwarder != null) logForwarder.stop();
        if (actionLogWriter != null) actionLogWriter.close();
    }

    public ActionLog currentActionLog() {
        ActionLogger logger = actionLoggers.get();
        if (logger == null) return null;
        return logger.log;
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
