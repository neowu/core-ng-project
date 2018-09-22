package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.PerformanceStatMessage;
import core.framework.impl.log.message.StatMessage;
import core.framework.util.Maps;
import core.framework.util.Network;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class MessageFactory {
    private static final int MAX_TRACE_LENGTH = 900000; // 900K, kafka max message size is 1M, leave 100K for rest, assume majority chars is in ascii (one char = one byte)

    public static ActionLogMessage actionLog(ActionLog log, String appName, LogFilter filter) {
        var message = new ActionLogMessage();
        message.app = appName;
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
        Map<String, PerformanceStatMessage> performanceStats = Maps.newHashMapWithExpectedSize(log.performanceStats.size());
        for (Map.Entry<String, PerformanceStat> entry : log.performanceStats.entrySet()) {
            PerformanceStat stat = entry.getValue();
            var statMessage = new PerformanceStatMessage();
            statMessage.count = stat.count;
            statMessage.totalElapsed = stat.totalElapsed;
            statMessage.readEntries = stat.readEntries;
            statMessage.writeEntries = stat.writeEntries;
            performanceStats.put(entry.getKey(), statMessage);
        }
        message.performanceStats = performanceStats;
        if (log.flushTraceLog()) {
            message.traceLog = trace(log, filter, MAX_TRACE_LENGTH);
        }
        return message;
    }

    static String trace(ActionLog log, LogFilter filter, int maxLength) {
        var builder = new StringBuilder(log.events.size() << 7);  // length * 128 as rough initial capacity
        for (LogEvent event : log.events) {
            event.trace(builder, log.startTime, filter);
            if (builder.length() >= maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                break;
            }
        }
        return builder.toString();
    }

    public static StatMessage stat(Map<String, Double> stats, String appName) {
        var message = new StatMessage();
        var now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.app = appName;
        message.serverIP = Network.localHostAddress();
        message.stats = stats;
        return message;
    }
}
