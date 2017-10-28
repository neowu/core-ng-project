package core.log;

import core.framework.module.App;
import core.framework.util.Strings;
import core.log.domain.ActionDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.service.ActionService;
import core.log.service.IndexService;
import core.log.service.KafkaConsumerFactory;
import core.log.service.MessageProcessor;
import core.log.service.StatService;

import java.time.LocalTime;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        loadProperties("sys.properties");

        search().host(esHost());
        search().type(ActionDocument.class);
        search().type(TraceDocument.class);
        search().type(StatDocument.class);

        bind(IndexService.class);
        bind(ActionService.class);
        bind(StatService.class);

        bind(KafkaConsumerFactory.class, new KafkaConsumerFactory(kafkaURI()));
        MessageProcessor processor = bind(MessageProcessor.class);
        processor.initialize();
        onStartup(processor::start);
        onShutdown(processor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }

    private String kafkaURI() {
        String uri = System.getenv("KAFKA_URI");
        if (!Strings.isEmpty(uri)) return uri;

        return requiredProperty("sys.kafka.uri");
    }

    private String esHost() {
        String host = System.getenv("ELASTICSEARCH_HOST");
        if (!Strings.isEmpty(host)) return host;

        return requiredProperty("sys.elasticsearch.host");
    }
}
