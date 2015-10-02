package core.framework.impl.log;

import core.framework.api.log.Warning;
import org.slf4j.Logger;

/**
 * @author neo
 */
public class LogManager {
    Logger logger;
    public final String appName;

    private final ThreadLocal<ActionLog> actionLog = new ThreadLocal<>();
    private final ThreadLocal<TraceLogger> traceLogger = new ThreadLocal<>();

    public TraceLoggerFactory traceLoggerFactory;
    public LogForwarder logForwarder;
    public ActionLogger actionLogger;

    public LogManager() {
        this.appName = System.getProperty("core.appName");
    }

    public void begin(String message) {
        ActionLog actionLog = new ActionLog();
        this.actionLog.set(actionLog);

        if (traceLoggerFactory != null) {
            traceLogger.set(traceLoggerFactory.create(actionLog, logForwarder));
            logger.debug(message);     // if trace log is disabled, then no need to process debug log
            logger.debug("[context] id={}", actionLog.id);
        }
    }

    public void end(String message) {
        logger.debug(message);

        ActionLog actionLog = currentActionLog();
        this.actionLog.remove();

        actionLog.end();

        if (actionLogger != null) actionLogger.write(actionLog);
        if (logForwarder != null) logForwarder.forwardActionLog(actionLog);

        if (traceLoggerFactory != null) {
            TraceLogger traceLogger = this.traceLogger.get();
            this.traceLogger.remove();
            traceLogger.close();
        }
    }

    public void process(LogEvent event) {
        ActionLog actionLog = currentActionLog();
        if (actionLog != null)
            actionLog.updateResult(event.level); // process is called by loggerImpl.log, begin() may not be called

        TraceLogger traceLogger = this.traceLogger.get();
        if (traceLogger != null) traceLogger.process(event);
    }

    public void start() {
        if (logForwarder != null) logForwarder.start();
    }

    public void stop() {
        if (logForwarder != null) logForwarder.stop();
        if (actionLogger != null) actionLogger.close();
    }

    public ActionLog currentActionLog() {
        return actionLog.get();
    }

    public void logError(Throwable e) {
        // write exception first, to avoid hiding it by other mistake
        String errorMessage = e.getMessage();
        if (e.getClass().isAnnotationPresent(Warning.class)) {
            logger.warn(errorMessage, e);
        } else {
            logger.error(errorMessage, e);
        }

        ActionLog actionLog = currentActionLog();
        actionLog.error(e);     // actionLog should not be null, logManager.begin should always be called before logError
    }
}
