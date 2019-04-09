package core.framework.impl.log;

import core.framework.internal.log.message.ActionLogMessage;
import core.framework.util.Network;

import static core.framework.impl.log.LogLevel.WARN;

/**
 * @author neo
 */
public class ActionLogMessageFactory {
    private static final int MAX_TRACE_LENGTH = 900000; // hard limitation of trace, 900K, kafka max message size is 1M, leave 100K for rest, assume majority chars is in ascii (one char = one byte)
    private static final int MAX_DEBUG_TRACE_LENGTH = 600000; // 600K assume majority chars is in ascii (one char = one byte)

    public ActionLogMessage create(ActionLog log) {
        var message = new ActionLogMessage();
        message.app = LogManager.APP_NAME;
        message.serverIP = Network.LOCAL_HOST_ADDRESS;
        message.id = log.id;
        message.date = log.date;
        message.result = log.result();
        message.correlationIds = log.correlationIds;
        message.clients = log.clients;
        message.refIds = log.refIds;
        message.elapsed = log.elapsed;
        message.cpuTime = log.cpuTime;
        message.action = log.action;
        message.errorCode = log.errorCode();
        message.errorMessage = log.errorMessage;
        message.context = log.context;
        message.stats = log.stats;
        message.performanceStats = log.performanceStats;
        if (log.flushTraceLog()) {
            message.traceLog = trace(log, MAX_DEBUG_TRACE_LENGTH, MAX_TRACE_LENGTH);
        }
        return message;
    }

    String trace(ActionLog log, int maxDebugLength, int maxLength) {
        var builder = new StringBuilder(log.events.size() << 7);  // length * 128 as rough initial capacity
        boolean reachedMaxDebugLength = false;
        for (LogEvent event : log.events) {
            if (!reachedMaxDebugLength) {
                event.appendTrace(builder, log.startTime);
                if (builder.length() >= maxDebugLength) {
                    reachedMaxDebugLength = true;
                    builder.setLength(event.level.value < LogLevel.WARN.value ? maxDebugLength : builder.length());
                    builder.append("...(max trace length reached)\n");
                }
            } else if (event.level.value >= WARN.value) {
                event.appendTrace(builder, log.startTime);
                if (builder.length() >= maxLength) {
                    builder.setLength(maxLength);
                    builder.append("...(truncated)");
                    break;
                }
            }
        }
        return builder.toString();
    }
}
