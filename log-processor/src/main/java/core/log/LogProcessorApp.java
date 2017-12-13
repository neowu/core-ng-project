package core.log;

import core.framework.impl.log.stat.Stat;
import core.framework.module.App;
import core.log.domain.ActionDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.job.CollectStatJob;
import core.log.service.ActionService;
import core.log.service.IndexService;
import core.log.service.KafkaConsumerFactory;
import core.log.service.MessageProcessor;
import core.log.service.StatService;

import java.time.Duration;
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

        bind(IndexService.class);
        bind(ActionService.class);
        bind(StatService.class);

        Stat stat = bind(new Stat());

        bind(KafkaConsumerFactory.class, new KafkaConsumerFactory(requiredProperty("sys.kafka.uri")));
        MessageProcessor processor = bind(MessageProcessor.class);
        processor.initialize();
        stat.metrics.add(processor.metrics);
        onStartup(processor::start);
        onShutdown(processor::stop);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
        schedule().fixedRate("collect-stat-job", bind(CollectStatJob.class), Duration.ofSeconds(10));
    }
}
