package core.framework.internal.log;

import core.framework.util.Maps;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author neo
 */
public final class DefaultLoggerFactory implements ILoggerFactory {
    private final Map<String, Logger> loggers = Maps.newConcurrentHashMap();

    @Override
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, this::createLogger);
    }

    private Logger createLogger(String name) {
        return new LoggerImpl(name, infoLevel(name), traceLevel(name));
    }

    private LogLevel infoLevel(String name) {
        // kafka log info for every producer/consumer, to reduce verbosity
        if (name.startsWith("org.apache.kafka.")) {
            return LogLevel.WARN;
        }
        // refer to org.elasticsearch.nativeaccess.NativeAccessHolder, to emmit warning under integration-test env
        if (name.startsWith("org.elasticsearch.nativeaccess.")) {
            return LogLevel.ERROR;
        }
        return LogLevel.INFO;
    }

    private LogLevel traceLevel(String name) {
        if (name.startsWith("org.elasticsearch.")
            || name.startsWith("org.mongodb.")
            || name.startsWith("org.xnio.")
            || name.startsWith("org.apache.")) {
            return LogLevel.INFO;
        }
        return LogLevel.DEBUG;
    }
}
