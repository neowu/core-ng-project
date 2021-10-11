package core.framework.log;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public final class ActionLogContext {
    @Nullable
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

    // to collect numeric metrics, and can be aggregated by Elasticsearch/Kibana
    public static void stat(String key, double value) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog != null) {
            actionLog.stat(key, value);
        }
    }

    public static int track(String operation, long elapsed) {
        return track(operation, elapsed, 0, 0);
    }

    // return the total count of operations within current action
    public static int track(String operation, long elapsed, int readEntries, int writeEntries) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return 1;    // be called without action context
        return actionLog.track(operation, elapsed, readEntries, writeEntries);
    }

    @Nullable
    public static Duration remainingProcessTime() {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return null;
        return Duration.ofNanos(actionLog.remainingProcessTimeInNano());
    }

    // return complete trace at this point, be aware of when you call this within action, action hasn't ended yet
    @Nullable
    public static String trace() {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return null;
        return actionLog.trace(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}
