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
    private static final int MAX_TRACE_LENGTH = 1000000; // 1M

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
            var builder = new StringBuilder(log.events.size() << 8);  // length * 256 as rough initial capacity
            for (LogEvent event : log.events) {
                String traceMessage = event.trace(log.startTime, filter);
                if (builder.length() + traceMessage.length() >= MAX_TRACE_LENGTH) {
                    builder.append(traceMessage, 0, MAX_TRACE_LENGTH - builder.length());
                    builder.append("...(truncated)");
                    break;
                }
                builder.append(traceMessage);
            }
            message.traceLog = builder.toString();
        }
        return message;
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
