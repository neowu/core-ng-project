package core.framework.internal.log;

import core.framework.internal.log.filter.LogFilter;
import core.framework.log.ErrorCode;
import core.framework.log.LogAppender;
import core.framework.log.Markers;
import core.framework.log.Severity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.Map;

import static java.lang.ScopedValue.where;

/**
 * @author neo
 */
public class LogManager {
    public static final String APP_NAME;

    public static final IdGenerator ID_GENERATOR = new IdGenerator();
    static final LogFilter FILTER = new LogFilter();
    private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);
    private static final ScopedValue<ActionLog> CURRENT_ACTION_LOG = ScopedValue.newInstance();

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

    @Nullable
    public static ActionLog currentActionLog() {
        if (!CURRENT_ACTION_LOG.isBound()) return null;
        return CURRENT_ACTION_LOG.get();
    }

    private final ActionLogMessageFactory actionLogMessageFactory = new ActionLogMessageFactory();
    @Nullable
    public LogAppender appender;

    // this is internal api, to simplify the design, task must not throw exceptions, all callers catch throwable and log error
    // if task throws exception, current action log may not be marked as error
    public <T> T run(String action, @Nullable String id, ActionLogCallable<T> task) {
        var log = new ActionLog("=== " + action + " begin ===", id);
        return where(CURRENT_ACTION_LOG, log).call(() -> {
            try {
                return task.call(log);
            } finally {
                log.end("=== " + action + " end ===");
                appendLog(log);
            }
        });
    }

    private void appendLog(ActionLog log) {
        if (appender != null) {
            try {
                appender.append(actionLogMessageFactory.create(log));
            } catch (Throwable e) {
                LOGGER.warn("failed to append action log, error={}", e.getMessage(), e);
            }
        }
    }

    public void logError(Throwable e) {
        String errorMessage = e.getMessage();
        Marker marker = Markers.errorCode(errorCode(e));
        if (e instanceof ErrorCode errorCode && errorCode.severity() == Severity.WARN) {
            LOGGER.warn(marker, errorMessage, e);
        } else {
            LOGGER.error(marker, errorMessage, e);
        }
    }

    String errorCode(Throwable e) {
        return e instanceof ErrorCode errorCode ? errorCode.errorCode() : e.getClass().getCanonicalName();
    }

    public void maskFields(String... fields) {
        Collections.addAll(FILTER.maskedFields, fields);
    }

    @FunctionalInterface
    public interface ActionLogCallable<T> {
        T call(ActionLog actionLog);
    }
}
