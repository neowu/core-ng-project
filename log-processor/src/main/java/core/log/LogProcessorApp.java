package core.log;

import core.framework.api.App;
import core.framework.api.module.SystemModule;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.TraceLogMessage;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;
import core.log.queue.ActionLogMessageHandler;
import core.log.queue.TraceLogMessageHandler;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        search().type(ActionLogDocument.class);
        search().type(TraceLogDocument.class);

        queue().subscribe("rabbitmq://queue/action-log-queue")
            .handle(ActionLogMessage.class, bind(ActionLogMessageHandler.class))
            .maxConcurrentHandlers(1);

        queue().subscribe("rabbitmq://queue/trace-log-queue")
            .handle(TraceLogMessage.class, bind(TraceLogMessageHandler.class))
            .maxConcurrentHandlers(1);
    }
}
