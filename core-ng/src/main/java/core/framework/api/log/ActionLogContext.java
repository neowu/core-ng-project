package core.framework.api.log;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.DefaultLoggerFactory;
import core.framework.impl.log.LogManager;
import org.slf4j.impl.StaticLoggerBinder;

import java.util.Optional;

/**
 * @author neo
 */
public final class ActionLogContext {
    public static Optional<String> get(String key) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return Optional.empty();
        return actionLog.getContext(key);
    }

    public static void put(String key, Object value) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog != null) {
            actionLog.putContext(key, value);
        }
    }

    public static void track(String action, long elapsedTime) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog != null) {
            actionLog.track(action, elapsedTime);
        }
    }

    private static LogManager logManager() {
        return ((DefaultLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory()).logManager;
    }
}
