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
        loadProperties("app.properties");

        search().type(ActionDocument.class);
        search().type(TraceDocument.class);
        search().type(StatDocument.class);

        ActionManager actionManager = bind(ActionManager.class);
        StatManager statManager = bind(StatManager.class);

        RabbitMQ rabbitMQ = new RabbitMQ();
        rabbitMQ.hosts(requiredProperty("app.rabbitMQ.host"));
        onShutdown(rabbitMQ::close);

        BulkMessageProcessor<ActionLogMessage> actionProcessor = new BulkMessageProcessor<>(rabbitMQ, "action-log-queue", ActionLogMessage.class, 2000, actionManager::index);
        onStartup(actionProcessor::start);
        onShutdown(actionProcessor::stop);

        BulkMessageProcessor<StatMessage> statProcessor = new BulkMessageProcessor<>(rabbitMQ, "stat-queue", StatMessage.class, 2000, statManager::index);
        onStartup(statProcessor::start);
        onShutdown(statProcessor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }
}
