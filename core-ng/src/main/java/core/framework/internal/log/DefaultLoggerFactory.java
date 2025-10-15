package core.framework.internal.log;

import core.framework.log.LogLevel;
import core.framework.log.LogLevels;
import core.framework.util.Maps;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author neo
 */
public final class DefaultLoggerFactory implements ILoggerFactory {
    private final Map<String, Logger> loggers = Maps.newConcurrentHashMap();
    private final LogLevels.Entry[] infoLevels;
    private final LogLevels.Entry[] traceLevels;

    public DefaultLoggerFactory(LogLevels.Entry[] infoLevels, LogLevels.Entry[] traceLevels) {
        this.infoLevels = infoLevels;
        this.traceLevels = traceLevels;
    }

    @Override
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, this::createLogger);
    }

    private Logger createLogger(String name) {
        return new LoggerImpl(name, infoLevel(name), traceLevel(name));
    }

    private LogLevel infoLevel(String name) {
        for (LogLevels.Entry entry : infoLevels) {
            if (name.startsWith(entry.prefix())) {
                return entry.level();
            }
        }
        return LogLevel.INFO;
    }

    private LogLevel traceLevel(String name) {
        for (LogLevels.Entry entry : traceLevels) {
            if (name.startsWith(entry.prefix())) {
                return entry.level();
            }
        }
        return LogLevel.DEBUG;
    }
}
