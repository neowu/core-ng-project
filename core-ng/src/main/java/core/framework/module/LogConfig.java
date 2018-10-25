package core.framework.module;

import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;
import core.framework.internal.log.CollectStatTask;
import core.framework.internal.log.appender.ConsoleAppender;
import core.framework.internal.log.appender.KafkaAppender;
import core.framework.internal.log.appender.LogAppender;

import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class LogConfig extends Config {
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    public void appendToConsole() {
        appender(new ConsoleAppender());
    }

    public void appendToKafka(String kafkaURI) {
        var appender = new KafkaAppender(kafkaURI);
        appender(appender);
        context.startupHook.add(appender::start);
        context.shutdownHook.add(ShutdownHook.STAGE_8, appender::stop);
        context.stat.metrics.add(appender.producerMetrics);
    }

    public void appender(LogAppender appender) {
        if (context.logManager.appender != null) throw new Error(format("log appender is already set, appender={}", context.logManager.appender.getClass().getSimpleName()));
        context.logManager.appender = appender;
        context.backgroundTask().scheduleWithFixedDelay(new CollectStatTask(appender, context.stat), Duration.ofSeconds(10));
    }

    public void maskFields(String... fields) {
        context.logManager.maskFields(fields);
    }
}
