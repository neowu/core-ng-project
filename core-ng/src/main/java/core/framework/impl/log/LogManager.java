package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.log.ErrorCode;
import core.framework.log.Markers;
import core.framework.log.Severity;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * @author neo
 */
public final class LogManager {
    public final String appName;
    public final LogFilter filter = new LogFilter();
    private final ThreadLocal<ActionLog> actionLog = new ThreadLocal<>();
    private final Logger logger = new LoggerImpl(LoggerImpl.abbreviateLoggerName(LogManager.class.getCanonicalName()), this, LogLevel.INFO, LogLevel.DEBUG);
    public ConsoleAppender consoleAppender;
    public KafkaAppender kafkaAppender;

    public LogManager() {
        String appName = System.getProperty("core.appName");
        if (appName == null) {
            logger.info("not found -Dcore.appName, this should only happen in local dev env or test, use \"local\" as appName");
            appName = "local";
        }
        this.appName = appName;
    }

    public ActionLog begin(String message) {
        ActionLog actionLog = new ActionLog(message, filter);
        this.actionLog.set(actionLog);
        return actionLog;
    }

    public void end(String message) {
        ActionLog actionLog = currentActionLog();
        this.actionLog.remove();
        actionLog.end(message);

        if (consoleAppender != null) consoleAppender.write(actionLog);
        if (kafkaAppender != null) kafkaAppender.forward(actionLog);
    }

    public void process(LogEvent event) {
        ActionLog actionLog = currentActionLog();
        if (actionLog != null) actionLog.process(event);    // process is called by loggerImpl.log, begin() may not be called before
    }

    public ActionLog currentActionLog() {
        return actionLog.get();
    }

    public void logError(Throwable e) {
        String errorMessage = e.getMessage();
        String errorCode = errorCode(e);
        Marker marker = Markers.errorCode(errorCode);
        if (e instanceof ErrorCode && ((ErrorCode) e).severity() == Severity.WARN) {
            logger.warn(marker, errorMessage, e);
        } else {
            logger.error(marker, errorMessage, e);
        }
    }

    String errorCode(Throwable e) {
        return e instanceof ErrorCode ? ((ErrorCode) e).errorCode() : e.getClass().getCanonicalName();
    }
}
