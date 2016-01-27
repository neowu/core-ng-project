package core.log;

import core.framework.api.App;
import core.framework.api.module.SystemModule;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.queue.ActionLogProcessor;
import core.log.service.ActionLogManager;

import java.time.LocalTime;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");

        search().type(ActionLogDocument.class);
        search().type(TraceLogDocument.class);

        ActionLogManager actionLogManager = bind(ActionLogManager.class);

        ActionLogProcessor processor = new ActionLogProcessor(requiredProperty("app.rabbitMQ.host"), actionLogManager);
        onStartup(processor::start);
        onShutdown(processor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }
}
