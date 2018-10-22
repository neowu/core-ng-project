package core.framework.impl.log;

import core.framework.internal.log.message.ActionLogMessage;
import core.framework.util.Network;

/**
 * @author neo
 */
public class ActionLogMessageFactory {
    private static final int MAX_TRACE_LENGTH = 900000; // 900K, kafka max message size is 1M, leave 100K for rest, assume majority chars is in ascii (one char = one byte)

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
            message.traceLog = trace(log, MAX_TRACE_LENGTH);
        }
        return message;
    }

    String trace(ActionLog log, int maxLength) {
        var builder = new StringBuilder(log.events.size() << 7);  // length * 128 as rough initial capacity
        for (LogEvent event : log.events) {
            event.appendTrace(builder, log.startTime);
            if (builder.length() >= maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                break;
            }
        }
        return builder.toString();
    }
}
