package core.framework.module;

import core.framework.impl.log.ConsoleAppender;
import core.framework.impl.log.KafkaAppender;
import core.framework.impl.log.stat.CollectStatTask;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;

import java.time.Duration;
import java.util.Arrays;

/**
 * @author neo
 */
public class LogConfig extends Config {
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    @Override
    protected void validate() {
    }

    public void writeToConsole() {
        context.logManager.consoleAppender = new ConsoleAppender();
    }

    public void writeToKafka(String kafkaURI) {
        KafkaAppender appender = KafkaAppender.create(kafkaURI, context.logManager.appName);
        context.logManager.kafkaAppender = appender;
        context.startupHook.add(appender::start);
        context.shutdownHook.add(ShutdownHook.STAGE_3, appender::stop);

        context.stat.metrics.add(context.logManager.kafkaAppender.producerMetrics);
        context.backgroundTask().scheduleWithFixedDelay(new CollectStatTask(context.logManager.kafkaAppender, context.stat), Duration.ofSeconds(10));
    }

    public void maskFields(String... fields) {
        context.logManager.filter.maskedFields.addAll(Arrays.asList(fields));
    }
}
