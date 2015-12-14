package core.framework.impl.log;

import core.framework.api.log.Markers;
import core.framework.api.log.Warning;
import core.framework.impl.log.marker.ErrorTypeMarker;
import org.slf4j.Logger;

/**
 * @author neo
 */
public final class LogManager {
    public final String appName;
    private final ThreadLocal<ActionLog> actionLog = new ThreadLocal<>();
    private final ThreadLocal<TraceLogger> traceLogger = new ThreadLocal<>();
    public TraceLoggerFactory traceLoggerFactory;
    public LogForwarder logForwarder;
    public ActionLogger actionLogger;
    Logger logger;

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
        if (event.isWarningOrError() || event.trace()) {
            ActionLog actionLog = currentActionLog();
            if (actionLog != null) actionLog.process(event);    // process is called by loggerImpl.log, begin() may not be called
        }

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

    public void triggerTraceLog() {
        ActionLog actionLog = currentActionLog();   // actionLog should not be null, logManager.begin should always be called before triggerTraceLog
        logger.debug(Markers.TRACE, "trigger trace log, id={}, action={}", actionLog.id, actionLog.action);
    }

    public void logError(Throwable e) {
        // write exception first, to avoid hiding it by other mistake
        String errorMessage = e.getMessage();
        ErrorTypeMarker errorType = Markers.errorType(e.getClass().getCanonicalName());
        if (e.getClass().isAnnotationPresent(Warning.class)) {
            logger.warn(errorType, errorMessage, e);
        } else {
            logger.error(errorType, errorMessage, e);
        }
    }
}
