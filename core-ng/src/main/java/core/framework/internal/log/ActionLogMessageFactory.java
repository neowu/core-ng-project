package core.framework.internal.log;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.util.Maps;
import core.framework.util.Network;

import java.util.Map;

import static core.framework.internal.log.LogLevel.WARN;

/**
 * @author neo
 */
public class ActionLogMessageFactory {
    private static final int HARD_TRACE_LIMIT = 900000; // 900K, kafka max message size is 1M, leave 100K for rest, assume majority chars is in ascii (one char = one byte)
    private static final int SOFT_TRACE_LIMIT = 600000; // 600K assume majority chars is in ascii (one char = one byte)

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
            message.traceLog = trace(log, SOFT_TRACE_LIMIT, HARD_TRACE_LIMIT);
        }
        return message;
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

    String trace(ActionLog log, int softLimit, int hardLimit) {
        var builder = new StringBuilder(log.events.size() << 7);  // length * 128 as rough initial capacity
        boolean softLimitReached = false;
        for (LogEvent event : log.events) {
            if (!softLimitReached || event.level.value >= WARN.value) { // after soft limit, only write warn+ event
                event.appendTrace(builder, log.startTime);
            }

            if (!softLimitReached && builder.length() >= softLimit) {
                softLimitReached = true;
                if (event.level.value < LogLevel.WARN.value) builder.setLength(softLimit);  // do not truncate if current is warn
                builder.append("...(soft trace limit reached)\n");
            } else if (builder.length() >= hardLimit) {
                builder.setLength(hardLimit);
                builder.append("...(hard trace limit reached)");
                break;
            }
        }
        return builder.toString();
    }
}
