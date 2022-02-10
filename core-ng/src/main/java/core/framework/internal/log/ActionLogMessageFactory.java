package core.framework.internal.log;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.util.Maps;
import core.framework.util.Network;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ActionLogMessageFactory {
    // 900K, kafka max message size is 1024 * 1024, leave 100K for rest, assume majority chars is in ascii (one char = one byte)
    // refer to org.apache.kafka.clients.producer.ProducerConfig.MAX_REQUEST_SIZE_CONFIG
    private static final int HARD_TRACE_LIMIT = 900_000;
    // 600K assume majority chars is in ascii (one char = one byte)
    private static final int SOFT_TRACE_LIMIT = 600_000;

    public ActionLogMessage create(ActionLog log) {
        var message = new ActionLogMessage();
        message.app = LogManager.APP_NAME;
        message.host = Network.LOCAL_HOST_NAME;
        message.id = log.id;
        message.date = log.date;
        message.result = log.result();
        message.correlationIds = log.correlationIds;
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
            // balance precision and cost, only estimate rough size limit
            int contextSize = Math.min(estimatedSize(log.context), 300_000);    // leave some room for trace if context is too large
            message.traceLog = log.trace(SOFT_TRACE_LIMIT - contextSize, HARD_TRACE_LIMIT - contextSize);
        }
        return message;
    }

    int estimatedSize(Map<String, List<String>> context) {
        int size = 0;
        for (Map.Entry<String, List<String>> entry : context.entrySet()) {
            size += entry.getKey().length();
            for (String value : entry.getValue()) {
                // MessageListener may put null in context.key
                if (value != null) size += value.length();
            }
        }
        return size;
    }

    private Map<String, PerformanceStatMessage> performanceStats(Map<String, PerformanceStat> stats) {
        Map<String, PerformanceStatMessage> messages = Maps.newHashMapWithExpectedSize(stats.size());
        for (Map.Entry<String, PerformanceStat> entry : stats.entrySet()) {
            PerformanceStat value = entry.getValue();
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
