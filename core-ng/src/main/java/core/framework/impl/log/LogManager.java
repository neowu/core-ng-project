package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.log.ErrorCode;
import core.framework.log.Markers;
import core.framework.log.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * @author neo
 */
public class LogManager {
    public static final ThreadLocal<ActionLog> ACTION_LOG = new ThreadLocal<>();
    static final ActionIdGenerator ID_GENERATOR = new ActionIdGenerator();

    public final String appName;
    public final LogFilter filter = new LogFilter();
    private final Logger logger = LoggerFactory.getLogger(LogManager.class);
    public Appender appender;

    public LogManager() {
        String appName = System.getProperty("core.appName");
        if (appName == null) {
            logger.info("not found -Dcore.appName, this should only happen in local dev env or test, use \"local\" as appName");
            appName = "local";
        }
        this.appName = appName;
    }

    public ActionLog begin(String message) {
        var actionLog = new ActionLog(message);
        ACTION_LOG.set(actionLog);
        return actionLog;
    }

    public void end(String message) {
        ActionLog actionLog = ACTION_LOG.get();
        ACTION_LOG.remove();
        actionLog.end(message);

        if (appender != null) appender.append(actionLog, filter);
    }

    public ActionLog currentActionLog() {
        return ACTION_LOG.get();
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
