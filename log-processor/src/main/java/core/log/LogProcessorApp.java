package core.log;

import core.framework.api.AbstractApplication;
import core.framework.api.module.SystemModule;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.impl.log.ActionLogMessage;
import core.log.queue.ActionLogMessageHandler;

/**
 * @author neo
 */
public class LogProcessorApp extends AbstractApplication {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        loadProperties("app.properties");

        ElasticSearch search = bindSupplier(ElasticSearch.class, null, new ElasticSearchBuilder()
            .remote(requiredProperty("app.elasticSearchHost"))
            .index("action"));
        onShutdown(search::shutdown);

        queue().subscribe("rabbitmq://queue/action-log-queue")
            .handle(ActionLogMessage.class, bind(ActionLogMessageHandler.class))
            .maxConcurrentHandlers(1);

    }
}
