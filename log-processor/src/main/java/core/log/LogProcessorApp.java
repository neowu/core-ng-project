package core.log;

import core.framework.api.App;
import core.framework.api.module.SystemModule;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.StatMessage;
import core.framework.impl.queue.RabbitMQ;
import core.log.domain.ActionDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.queue.BulkMessageProcessor;
import core.log.service.ActionManager;
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

        queue().poolSize(0, 5); // disable publisher channel pool

        RabbitMQ rabbitMQ = bean(RabbitMQ.class);

        // our regular action logs are about 1.5M per 2000 messages
        BulkMessageProcessor<ActionLogMessage> actionProcessor = new BulkMessageProcessor<>(rabbitMQ, "action-log-queue", ActionLogMessage.class, 3000, actionManager::index);
        onStartup(actionProcessor::start);
        onShutdown(actionProcessor::stop);

        BulkMessageProcessor<StatMessage> statProcessor = new BulkMessageProcessor<>(rabbitMQ, "stat-queue", StatMessage.class, 1000, statManager::index);
        onStartup(statProcessor::start);
        onShutdown(statProcessor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }
}
