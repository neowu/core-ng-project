package core.framework.internal.log;

import core.framework.log.message.ActionLogMessage;
import core.framework.util.Network;

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
            message.traceLog = trace(log, SOFT_TRACE_LIMIT, HARD_TRACE_LIMIT);
        }
        return message;
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
