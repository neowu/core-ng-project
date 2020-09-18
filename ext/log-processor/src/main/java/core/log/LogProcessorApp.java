package core.log;

import core.framework.http.HTTPClient;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.App;
import core.framework.search.module.SearchConfig;
import core.log.domain.ActionDocument;
import core.log.domain.EventDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.kafka.ActionLogMessageHandler;
import core.log.kafka.EventMessageHandler;
import core.log.kafka.StatMessageHandler;
import core.log.service.ActionService;
import core.log.service.ElasticSearchAppender;
import core.log.service.EventService;
import core.log.service.IndexOption;
import core.log.service.IndexService;
import core.log.service.KibanaService;
import core.log.service.StatService;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        loadProperties("sys.properties");
        loadProperties("kibana.properties");
        loadProperties("index.properties");

        configureSearch();

        configureIndexOption();
        IndexService indexService = bind(IndexService.class);
        bind(ActionService.class);
        bind(StatService.class);
        bind(EventService.class);

        configureLogAppender();
        onStartup(indexService::createIndexTemplatesUntilSuccess);

        configureKibanaService();

        configureKafka();

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }

    private void configureKibanaService() {
        // explicitly use all properties in case runtime env doesn't define any, otherwise startup may fail with unused property error
        Optional<String> kibanaURL = property("kibana.url");
        String banner = property("kibana.banner").orElse("");
        kibanaURL.ifPresent(url -> {
            HTTPClient client = HTTPClient.builder().maxRetries(5).build();  // create ad hoc http client, will be recycled once done
            onStartup(() -> new Thread(new KibanaService(url, banner, client)::importObjects, "kibana").start());
        });
    }

    private void configureLogAppender() {
        property("sys.log.appender").ifPresent(appender -> {
            if ("console".equals(appender)) {
                log().appendToConsole();
            } else if ("elasticsearch".equals(appender)) {
                log().appender(bind(ElasticSearchAppender.class));  // ElasticSearchAppender doesn't need to stop, es will be stopped at stage 7
            }
        });
    }

    private void configureIndexOption() {
        var option = new IndexOption();
        option.numberOfShards = Integer.parseInt(property("index.shards").orElse("1"));   // with small cluster one shard has best performance, for larger cluster use kube env to customize
        option.refreshInterval = property("index.refresh.interval").orElse("10s");  // use longer refresh to tune load on log es
        bind(option);
    }

    private void configureKafka() {
        kafka().uri(requiredProperty("sys.kafka.uri"));
        kafka().poolSize(Runtime.getRuntime().availableProcessors() == 1 ? 1 : 2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get at least 1M message
        kafka().maxPoll(2000, 3 * 1024 * 1024);     // get 3M message at max
        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(EventMessageHandler.class));
    }

    private void configureSearch() {
        SearchConfig search = config(SearchConfig.class);
        search.host(requiredProperty("sys.elasticsearch.host"));
        search.timeout(Duration.ofSeconds(20)); // use longer timeout/slowES threshold as log indexing can be slower with large batches
        search.slowOperationThreshold(Duration.ofSeconds(10));
        search.type(ActionDocument.class);
        search.type(TraceDocument.class);
        search.type(StatDocument.class);
        search.type(EventDocument.class);
    }
}
