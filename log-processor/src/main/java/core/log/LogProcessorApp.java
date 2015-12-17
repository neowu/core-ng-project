package core.log;

import core.framework.api.App;
import core.framework.api.module.SystemModule;
import core.framework.impl.log.queue.ActionLogMessages;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;
import core.log.queue.ActionLogMessagesHandler;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        search().type(ActionLogDocument.class);
        search().type(TraceLogDocument.class);

        // with typical t2.medium/t2.large setup, by bulkIndex, all requests will be queued up in ES in bulk queue,
        // so no need to increase concurrency here
        queue().subscribe("rabbitmq://queue/action-log-queue")
            .handle(ActionLogMessages.class, bind(ActionLogMessagesHandler.class))
            .maxConcurrentHandlers(2);
    }
}
