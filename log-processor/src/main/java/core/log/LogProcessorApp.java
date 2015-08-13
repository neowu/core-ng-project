package core.log;

import core.framework.api.AbstractApplication;
import core.framework.api.module.SystemModule;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Types;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.TraceLogMessage;
import core.log.queue.ActionLogMessageHandler;
import core.log.queue.TraceLogMessageHandler;

/**
 * @author neo
 */
public class LogProcessorApp extends AbstractApplication {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        loadProperties("app.properties");

        ElasticSearch search = bindSupplier(ElasticSearch.class, null, new ElasticSearchBuilder()
            .remote(requiredProperty("app.elasticSearchHost")));
        onShutdown(search::shutdown);

        bind(Types.generic(ElasticSearchType.class, ActionLogMessage.class), null, search.type("action", "action", ActionLogMessage.class));
        bind(Types.generic(ElasticSearchType.class, TraceLogMessage.class), null, search.type("trace", "trace", TraceLogMessage.class));

        queue().subscribe("rabbitmq://queue/action-log-queue")
            .handle(ActionLogMessage.class, bind(ActionLogMessageHandler.class))
            .maxConcurrentHandlers(1);

        queue().subscribe("rabbitmq://queue/trace-log-queue")
            .handle(TraceLogMessage.class, bind(TraceLogMessageHandler.class))
            .maxConcurrentHandlers(1);
    }
}
