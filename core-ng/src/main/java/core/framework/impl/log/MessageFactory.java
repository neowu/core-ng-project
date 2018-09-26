package core.framework.impl.log;

import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.StatMessage;
import core.framework.util.Network;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class MessageFactory {
    private static final int MAX_TRACE_LENGTH = 900000; // 900K, kafka max message size is 1M, leave 100K for rest, assume majority chars is in ascii (one char = one byte)

    public static ActionLogMessage actionLog(ActionLog log) {
        var message = new ActionLogMessage();
        message.app = LogManager.APP_NAME;
        message.serverIP = Network.localHostAddress();
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

    static String trace(ActionLog log, int maxLength) {
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

    public static StatMessage stat(Map<String, Double> stats) {
        var message = new StatMessage();
        var now = Instant.now();
        message.date = now;
        message.id = LogManager.ID_GENERATOR.next(now);
        message.app = LogManager.APP_NAME;
        message.serverIP = Network.localHostAddress();
        message.stats = stats;
        return message;
    }
}
