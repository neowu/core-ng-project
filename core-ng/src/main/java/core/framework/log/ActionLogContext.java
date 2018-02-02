package core.framework.log;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.DefaultLoggerFactory;
import core.framework.impl.log.LogManager;
import org.slf4j.impl.StaticLoggerBinder;

import java.util.Optional;

/**
 * @author neo
 */
public final class ActionLogContext {
    public static String id() {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return null;
        return actionLog.id;
    }

    public static Optional<String> get(String key) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return Optional.empty();
        return actionLog.context(key);
    }

    public static void put(String key, Object value) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog != null) {    // here to check null is for unit testing the logManager.begin may not be called
            actionLog.context(key, value);
        }
    }

    // used to collect business metrics, and can be aggregated by Elasticsearch/Kibana
    public static void stat(String key, Number value) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog != null) {
            actionLog.stat(key, value);
        }
    }

    public static void track(String action, long elapsedTime) {
        track(action, elapsedTime, null, null);
    }

    public static void track(String action, long elapsedTime, Integer readEntries, Integer writeEntries) {
        LogManager logManager = logManager();
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog != null) {
            actionLog.track(action, elapsedTime, readEntries, writeEntries);
        }
    }

    private static LogManager logManager() {
        return ((DefaultLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory()).logManager;
    }
}
