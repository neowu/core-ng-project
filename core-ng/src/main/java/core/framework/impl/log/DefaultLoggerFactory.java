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
    public final ActionLogger actionLogger = new ActionLogger();

    @Override
    public Logger getLogger(String name) {
        Logger logger = loggers.get(name);
        if (logger != null) {
            return logger;
        } else {
            LogLevel[] levels = logLevel(name);
            logger = new LoggerImpl(name, actionLogger, levels[0], levels[1]);
            Logger existingLogger = loggers.putIfAbsent(name, logger);
            return existingLogger == null ? logger : existingLogger;
        }
    }

    private LogLevel[] logLevel(String name) {
        if (name.startsWith("com.mchange")
            || name.startsWith("org.thymeleaf")
            || name.startsWith("org.elasticsearch")
            || name.startsWith("org.mongodb")
            || name.startsWith("org.xnio")
            || name.startsWith("org.apache")) {
            return new LogLevel[]{LogLevel.WARN, LogLevel.INFO};
        }
        return new LogLevel[]{LogLevel.INFO, LogLevel.DEBUG};
    }

    public void shutdown() {
        Logger logger = getLogger(DefaultLoggerFactory.class.getName());
        logger.info("showdown log factory");
        actionLogger.close();
    }
}
