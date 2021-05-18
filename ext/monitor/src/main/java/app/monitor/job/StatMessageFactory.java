package app.monitor.job;

import core.framework.internal.log.LogManager;
import core.framework.internal.stat.Stats;
import core.framework.log.message.StatMessage;
import core.framework.util.Exceptions;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
class StatMessageFactory {
    static StatMessage stats(String app, String host, Stats stats) {
        var now = Instant.now();
        var message = new StatMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.app = app;
        message.host = host;
        message.result = stats.result();
        message.stats = stats.stats;
        message.errorCode = stats.errorCode;
        message.errorMessage = stats.errorMessage;
        return message;
    }

    static StatMessage failedToCollect(String app, String host, Throwable e) {
        var now = Instant.now();
        var message = new StatMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.result = "ERROR";
        message.app = app;
        message.host = host;
        message.errorCode = "FAILED_TO_COLLECT";
        message.errorMessage = e.getMessage();
        message.info = Map.of("stack_trace", Exceptions.stackTrace(e));
        return message;
    }
}
