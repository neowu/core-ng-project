package core.framework.log;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;

import java.util.List;

/**
 * @author neo
 */
public final class ActionLogContext {
    public static String id() {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return null;
        return actionLog.id;
    }

    public static List<String> get(String key) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return List.of();
        List<String> values = actionLog.context.get(key);
        if (values == null) return List.of();
        return values;
    }

    public static void put(String key, Object... values) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {    // here to check null is for unit testing the logManager.begin may not be called
            actionLog.context(key, values);
        }
    }

    // used to collect business metrics, and can be aggregated by Elasticsearch/Kibana
    public static void stat(String key, double value) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            actionLog.stat(key, value);
        }
    }

    public static void track(String action, long elapsed) {
        track(action, elapsed, null, null);
    }

    public static void track(String action, long elapsed, Integer readEntries, Integer writeEntries) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            actionLog.track(action, elapsed, readEntries, writeEntries);
        }
    }
}
