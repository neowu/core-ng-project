package core.framework.impl.log;

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
        if ("org.apache.kafka.clients.consumer.ConsumerConfig".equals(name)
                || "org.apache.kafka.clients.producer.ProducerConfig".equals(name)
                || "org.apache.kafka.clients.admin.AdminClientConfig".equals(name)
                || "org.apache.kafka.common.utils.AppInfoParser".equals(name)) {
            return LogLevel.WARN;
        }
        return LogLevel.INFO;
    }

    private LogLevel traceLevel(String name) {
        if (name.startsWith("org.elasticsearch")
                || name.startsWith("org.mongodb")
                || name.startsWith("org.apache")
                || name.startsWith("org.xnio")) {
            return LogLevel.INFO;
        }
        return LogLevel.DEBUG;
    }
}
