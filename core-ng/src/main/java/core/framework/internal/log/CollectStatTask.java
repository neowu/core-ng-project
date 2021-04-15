package core.framework.internal.log;

import core.framework.internal.stat.StatCollector;
import core.framework.internal.stat.Stats;
import core.framework.log.LogAppender;
import core.framework.log.message.StatMessage;
import core.framework.util.Network;

import java.time.Instant;

/**
 * @author neo
 */
public final class CollectStatTask implements Runnable {
    private final LogAppender appender;
    private final StatCollector collector;
    private int count;

    public CollectStatTask(LogAppender appender, StatCollector collector) {
        this.appender = appender;
        this.collector = collector;
    }

    @Override
    public void run() {
        var stats = new Stats();
        collector.collectJVMUsage(stats);
        collector.collectMetrics(stats);
        if (count % 6 == 0) {   // every 60s
            collector.collectMemoryUsage(stats);
        }
        count++;
        StatMessage message = message(stats);
        appender.append(message);
    }

    StatMessage message(Stats stats) {
        var message = new StatMessage();
        var now = Instant.now();
        message.date = now;
        message.id = LogManager.ID_GENERATOR.next(now);
        message.result = stats.result();
        message.app = LogManager.APP_NAME;
        message.host = Network.LOCAL_HOST_NAME;
        message.errorCode = stats.errorCode;
        message.errorMessage = stats.errorMessage;
        message.stats = stats.stats;
        message.info = stats.info;
        return message;
    }
}
