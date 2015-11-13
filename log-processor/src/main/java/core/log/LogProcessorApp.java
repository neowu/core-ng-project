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

        // with typical t2.medium/t2.large setup, 15/10 are optimal number to keep CPU around 50%~70% with large amount of messages
        queue().subscribe("rabbitmq://queue/action-log-queue")
            .handle(ActionLogMessage.class, bind(ActionLogMessageHandler.class))
            .maxConcurrentHandlers(15);

        queue().subscribe("rabbitmq://queue/trace-log-queue")
            .handle(TraceLogMessage.class, bind(TraceLogMessageHandler.class))
            .maxConcurrentHandlers(1);  // trace message is to append to existing message id, must be handled in single thread, otherwise ES will lost trace log
    }
}
