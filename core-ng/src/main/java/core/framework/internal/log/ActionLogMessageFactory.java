package core.framework.internal.log;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.util.Maps;
import core.framework.util.Network;

import java.util.Map;

/**
 * @author neo
 */
public class ActionLogMessageFactory {
    public ActionLogMessage create(ActionLog log) {
        var message = new ActionLogMessage();
        message.app = LogManager.APP_NAME;
        message.host = Network.LOCAL_HOST_NAME;
        message.id = log.id;
        message.date = log.date;
        message.result = log.result();
        message.correlationIds = log.correlationIds();
        message.clients = log.clients;
        message.refIds = log.refIds;
        message.elapsed = log.elapsed;
        message.action = log.action;
        message.errorCode = log.errorCode();
        message.errorMessage = log.errorMessage;
        message.context = log.context;
        message.stats = log.stats;
        message.performanceStats = performanceStats(log.performanceStats);
        if (log.flushTraceLog()) {
            message.traceLog = log.trace();
        }
        return message;
    }

    private Map<String, PerformanceStatMessage> performanceStats(Map<String, PerformanceStat> stats) {
        Map<String, PerformanceStatMessage> messages = Maps.newHashMapWithExpectedSize(stats.size());
        for (Map.Entry<String, PerformanceStat> entry : stats.entrySet()) {
            PerformanceStat value = entry.getValue();
            if (value.count == 0) continue;     // for IOWarnings, it may initialize warnings not used by current action (e.g. for executor task action)
            var message = new PerformanceStatMessage();
            message.count = value.count;
            message.totalElapsed = value.totalElapsed;
            if (value.readEntries != 0) message.readEntries = value.readEntries;
            if (value.writeEntries != 0) message.writeEntries = value.writeEntries;
            messages.put(entry.getKey(), message);
        }
        return messages;
    }
}
