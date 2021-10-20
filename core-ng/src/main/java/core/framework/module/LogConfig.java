package core.framework.module;

import core.framework.internal.kafka.KafkaURI;
import core.framework.internal.log.CollectStatTask;
import core.framework.internal.log.CompositeLogAppender;
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
        appender(new ConsoleAppender(), true);
    }

    public void appendToKafka(String uri) {
        var appender = new KafkaAppender(new KafkaURI(uri));
        appender(appender, true);
        context.startupHook.add(appender::start);
        context.shutdownHook.add(ShutdownHook.STAGE_8, appender::stop);
        context.collector.metrics.add(appender.producerMetrics);
    }

    public void appender(LogAppender appender) {
        appender(appender, false);
    }

    private void appender(LogAppender appender, boolean system) {
        if (context.logManager.appender == null) {
            context.logManager.appender = new CompositeLogAppender();
            context.backgroundTask().scheduleWithFixedDelay(new CollectStatTask(context.logManager.appender, context.collector), Duration.ofSeconds(10));
        }
        if (system) {
            if (context.logManager.appender.systemAppender != null)
                throw new Error("log appender is already set, appender=" + context.logManager.appender.systemAppender.getClass().getSimpleName());
            context.logManager.appender.systemAppender = appender;
        } else {
            if (context.logManager.appender.customAppender != null)
                throw new Error("log appender is already set, appender=" + context.logManager.appender.customAppender.getClass().getSimpleName());
            context.logManager.appender.customAppender = appender;
        }
    }

    public void maskFields(String... fields) {
        context.logManager.maskFields(fields);
    }
}
