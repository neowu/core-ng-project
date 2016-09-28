package core.log;

import core.framework.api.App;
import core.framework.api.module.SystemModule;
import core.log.domain.ActionDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.service.ActionManager;
import core.log.service.MessageProcessor;
import core.log.service.StatManager;

import java.time.LocalTime;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        search().type(ActionDocument.class);
        search().type(TraceDocument.class);
        search().type(StatDocument.class);

        ActionManager actionManager = bind(ActionManager.class);
        StatManager statManager = bind(StatManager.class);

        MessageProcessor processor = new MessageProcessor(requiredProperty("sys.kafka.host"), actionManager, statManager);
        onStartup(processor::start);
        onShutdown(processor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }
}
