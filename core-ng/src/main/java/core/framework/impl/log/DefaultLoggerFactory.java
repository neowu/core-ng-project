package core.framework.impl.log;

import core.framework.api.util.Maps;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author neo
 */
public class DefaultLoggerFactory implements ILoggerFactory {
    private final Map<String, Logger> loggers = Maps.newConcurrentHashMap();
    public final LogManager logManager;

    public DefaultLoggerFactory() {
        logManager = new LogManager();
        logManager.logger = getLogger(LogManager.class.getName());
    }

    @Override
    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, this::createLogger);
    }

    private Logger createLogger(String name) {
        LogLevel[] levels = logLevel(name);
        return new LoggerImpl(name, logManager, levels[0], levels[1]);
    }

    private LogLevel[] logLevel(String name) {
        if (name.startsWith("com.mchange")
            || name.startsWith("org.elasticsearch")
            || name.startsWith("org.mongodb")
            || name.startsWith("org.xnio")
            || name.startsWith("org.apache")) {
            return new LogLevel[]{LogLevel.WARN, LogLevel.INFO};
        }
        return new LogLevel[]{LogLevel.INFO, LogLevel.DEBUG};
    }
}
