package core.framework.internal.log;

import core.framework.internal.log.appender.LogAppender;
import core.framework.internal.log.filter.LogFilter;
import core.framework.log.ErrorCode;
import core.framework.log.Markers;
import core.framework.log.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author neo
 */
public class LogManager {
    public static final ThreadLocal<ActionLog> CURRENT_ACTION_LOG = new ThreadLocal<>();
    public static final String APP_NAME;

    public static final IdGenerator ID_GENERATOR = new IdGenerator();
    static final LogFilter FILTER = new LogFilter();
    private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);

    static {
        APP_NAME = appName(System.getenv());
    }

    static String appName(Map<String, String> env) {
        // gradle application plugin linux script uses APP_NAME as variable which shadowed env variable, so here to use different name
        // refer to https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
        String appName = env.get("CORE_APP_NAME");
        if (appName != null) {
            LOGGER.info("found CORE_APP_NAME env var, appName={}", appName);
            return appName;
        }
        appName = System.getProperty("core.appName");
        if (appName == null) {
            LOGGER.info("not found -Dcore.appName, this should only happen in local dev env or test, use \"local\" as appName");
            appName = "local";
        }
        return appName;
    }

    private final ActionLogMessageFactory actionLogMessageFactory = new ActionLogMessageFactory();
    public LogAppender appender;

    public ActionLog begin(String message, String id) {
        var actionLog = new ActionLog(message, id);
        CURRENT_ACTION_LOG.set(actionLog);
        return actionLog;
    }

    public void end(String message) {
        ActionLog actionLog = CURRENT_ACTION_LOG.get();
        checkSlowProcess(actionLog);
        CURRENT_ACTION_LOG.remove();
        actionLog.end(message);

        if (appender != null) {
            try {
                appender.append(actionLogMessageFactory.create(actionLog));
            } catch (Throwable e) {
                LOGGER.warn("failed to append action log, error={}", e.getMessage(), e);
            }
        }
    }

    void checkSlowProcess(ActionLog actionLog) {
        long maxProcessTimeInNano = actionLog.maxProcessTimeInNano;
        if (maxProcessTimeInNano > 0) {
            long elapsed = actionLog.elapsed();
            if (elapsed > maxProcessTimeInNano * 0.8) {
                LOGGER.warn(Markers.errorCode("LONG_PROCESS"), "action took more than 80% of max process time, maxProcessTime={}, elapsed={}", Duration.ofNanos(maxProcessTimeInNano), Duration.ofNanos(elapsed));
            }
        }
    }

    public void logError(Throwable e) {
        String errorMessage = e.getMessage();
        String errorCode = errorCode(e);
        Marker marker = Markers.errorCode(errorCode);
        if (e instanceof ErrorCode && ((ErrorCode) e).severity() == Severity.WARN) {
            LOGGER.warn(marker, errorMessage, e);
        } else {
            LOGGER.error(marker, errorMessage, e);
        }
    }

    String errorCode(Throwable e) {
        return e instanceof ErrorCode ? ((ErrorCode) e).errorCode() : e.getClass().getCanonicalName();
    }

    public void maskFields(String... fields) {
        Collections.addAll(FILTER.maskedFields, fields);
    }
}
