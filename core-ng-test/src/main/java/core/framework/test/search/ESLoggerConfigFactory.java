package core.framework.test.search;

import core.framework.api.util.Maps;
import core.framework.impl.log.LoggerImpl;
import core.framework.impl.search.log.ESLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
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
        PatternLayout layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, null, config, null, null, true, true, null, null);
        Appender appender = new AbstractAppender("", null, layout) {
            @Override
            public void append(LogEvent event) {
                String name = event.getLoggerName();
                ESLogger logger = loggers.computeIfAbsent(name, key -> new ESLogger(key, null, (LoggerImpl) LoggerFactory.getLogger(key)));
                logger.log(event.getLevel(), event.getMarker(), event.getMessage(), event.getThrown());
            }
        };
        appender.start();
        config.addAppender(appender);

        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, "", "true", new AppenderRef[0], null, config, null);
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
