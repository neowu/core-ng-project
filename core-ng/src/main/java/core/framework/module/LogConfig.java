package core.framework.module;

import core.framework.impl.log.ConsoleAppender;
import core.framework.impl.log.KafkaAppender;
import core.framework.impl.log.stat.CollectStatTask;
import core.framework.impl.module.ModuleContext;
import core.framework.log.MessageFilter;

import java.time.Duration;

/**
 * @author neo
 */
public final class LogConfig {
    private final ModuleContext context;

    LogConfig(ModuleContext context) {
        this.context = context;
    }

    public void writeToConsole() {
        if (!context.isTest()) {
            context.logManager.consoleAppender = new ConsoleAppender();
        }
    }

    public void writeToKafka(String kafkaURI) {
        if (!context.isTest()) {
            KafkaAppender appender = KafkaAppender.create(kafkaURI, context.logManager.appName);
            context.logManager.kafkaAppender = appender;
            context.startupHook.add(appender::start);
            context.shutdownHook.add(appender::stop);

            context.stat.metrics.add(context.logManager.kafkaAppender.producerMetrics);
            context.backgroundTask().scheduleWithFixedDelay(new CollectStatTask(context.logManager.kafkaAppender, context.stat), Duration.ofSeconds(10));
        }
    }

    public void filter(MessageFilter filter) {
        context.logManager.filter = filter;
    }
}
