package core.framework.module;

import core.framework.internal.kafka.KafkaURI;
import core.framework.internal.log.CollectStatTask;
import core.framework.internal.log.appender.ConsoleAppender;
import core.framework.internal.log.appender.KafkaAppender;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.log.LogAppender;

import java.time.Duration;

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

    public void appendToKafka(String uri) {
        var appender = new KafkaAppender(new KafkaURI(uri));
        appender(appender);
        context.startupHook.start.add(appender::start);
        context.shutdownHook.add(ShutdownHook.STAGE_7, appender::stop);
        context.collector.metrics.add(appender.producerMetrics);
    }

    public void appender(LogAppender appender) {
        if (context.logManager.appender != null) throw new Error("log appender is already set, appender=" + context.logManager.appender.getClass().getSimpleName());
        context.logManager.appender = appender;
        context.backgroundTask().scheduleWithFixedDelay(new CollectStatTask(appender, context.collector), Duration.ofSeconds(10));
    }

    public void maskFields(String... fields) {
        context.logManager.maskFields(fields);
    }
}
