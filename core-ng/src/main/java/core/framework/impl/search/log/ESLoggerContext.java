package core.framework.impl.search.log;

import core.framework.impl.log.LoggerImpl;
import core.framework.util.Maps;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.LoggerFactory;

import java.util.Map;

class ESLoggerContext implements LoggerContext {
    private final Map<String, ESLogger> loggers = Maps.newConcurrentHashMap();

    @Override
    public Object getExternalContext() {
        return null;
    }

    @Override
    public ExtendedLogger getLogger(String name) {
        return getLogger(name, null);
    }

    @Override
    public ExtendedLogger getLogger(String name, MessageFactory messageFactory) {
        return loggers.computeIfAbsent(name, key -> new ESLogger(key, messageFactory, (LoggerImpl) LoggerFactory.getLogger(key)));
    }

    @Override
    public boolean hasLogger(String name) {
        return loggers.containsKey(name);
    }

    @Override
    public boolean hasLogger(String name, MessageFactory messageFactory) {
        return hasLogger(name);
    }

    @Override
    public boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass) {
        return hasLogger(name);
    }
}
