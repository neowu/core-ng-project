package core.framework.module;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.ConsoleAppender;
import core.framework.impl.log.KafkaAppender;
import core.framework.impl.log.stat.CollectStatTask;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.module.ShutdownHook;

import java.time.Duration;
import java.util.function.Consumer;

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

    public void writeToConsole() {
        setLogAppender(new ConsoleAppender());
    }

    public void writeToKafka(String kafkaURI) {
        var appender = new KafkaAppender(kafkaURI);
        setLogAppender(appender);
        context.startupHook.add(appender::start);
        context.shutdownHook.add(ShutdownHook.STAGE_3, appender::stop);

        context.stat.metrics.add(appender.producerMetrics);
        context.backgroundTask().scheduleWithFixedDelay(new CollectStatTask(appender, context.stat), Duration.ofSeconds(10));
    }

    private void setLogAppender(Consumer<ActionLog> appender) {
        if (context.logManager.appender != null) throw new Error(format("log appender is already set, appender={}", context.logManager.appender.getClass().getSimpleName()));
        context.logManager.appender = appender;
    }

    public void maskFields(String... fields) {
        context.logManager.maskFields(fields);
    }
}
