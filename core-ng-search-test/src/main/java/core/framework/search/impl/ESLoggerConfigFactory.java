package core.framework.search.impl;

import core.framework.impl.log.LoggerImpl;
import core.framework.search.impl.log.ESLogger;
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
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

/**
 * @author neo
 */
public class ESLoggerConfigFactory extends ConfigurationFactory {  // due to elasticsearch refer to log4j impl, here is to bridge to coreng logger
    public static void bindLogger() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        Map<String, ESLogger> loggers = Maps.newConcurrentHashMap();
        Appender appender = new AbstractAppender("", null, null) {
            @Override
            public void append(LogEvent event) {
                String name = event.getLoggerName();
                ESLogger logger = loggers.computeIfAbsent(name, key -> new ESLogger(key, null, (LoggerImpl) LoggerFactory.getLogger(key)));
                logger.log(event.getLevel(), event.getMarker(), event.getMessage(), event.getThrown());
            }
        };
        appender.start();
        config.addAppender(appender);

        LoggerConfig loggerConfig = new LoggerConfig("", Level.ALL, false);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("", loggerConfig);
        context.updateLoggers();
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
