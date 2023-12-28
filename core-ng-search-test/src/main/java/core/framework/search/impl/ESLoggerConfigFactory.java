package core.framework.search.impl;

import core.framework.internal.log.LogLevel;
import core.framework.internal.log.LoggerImpl;
import core.framework.util.Maps;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

/**
 * @author neo
 */
public class ESLoggerConfigFactory extends ConfigurationFactory {  // due to elasticsearch refer to log4j impl, here is to bridge to coreng logger
    public static void configureLogger() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        Map<String, LoggerImpl> loggers = Maps.newConcurrentHashMap();
        Appender appender = new AbstractAppender("", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                String name = event.getLoggerName();
                LoggerImpl logger = loggers.computeIfAbsent(name, key -> (LoggerImpl) LoggerFactory.getLogger(key));
                logger.log(null, logLevel(event.getLevel()), event.getMessage().getFormattedMessage(), event.getMessage().getParameters(), event.getThrown());
            }
        };
        appender.start();
        config.addAppender(appender);

        var loggerConfig = new LoggerConfig("", Level.INFO, false); // only enable info and higher level
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("", loggerConfig);
        context.updateLoggers();
    }

    private static LogLevel logLevel(Level level) {
        return switch (level.getStandardLevel()) {
            case INFO -> LogLevel.INFO;
            case WARN -> LogLevel.WARN;
            case ERROR, FATAL -> LogLevel.ERROR;
            default -> LogLevel.DEBUG;
        };
    }

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{"*"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
        return getConfiguration(loggerContext, null);
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        return new DefaultConfiguration();
    }
}
