package core.log;

import core.framework.api.App;
import core.log.domain.ActionDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.service.ActionManager;
import core.log.service.KafkaConsumerFactory;
import core.log.service.MessageProcessor;
import core.log.service.StatManager;

import java.time.LocalTime;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        loadProperties("sys.properties");

        search().host(requiredProperty("sys.elasticsearch.host"));
        search().type(ActionDocument.class);
        search().type(TraceDocument.class);
        search().type(StatDocument.class);

        bind(ActionManager.class);
        bind(StatManager.class);

        bind(KafkaConsumerFactory.class, new KafkaConsumerFactory(requiredProperty("sys.kafka.uri")));
        MessageProcessor processor = bind(MessageProcessor.class);
        onStartup(processor::start);
        onShutdown(processor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }
}
