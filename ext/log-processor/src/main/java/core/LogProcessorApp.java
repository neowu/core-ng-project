package core;

import core.framework.http.HTTPClient;
import core.framework.json.Bean;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.framework.search.module.SearchConfig;
import core.log.LogForwardConfig;
import core.log.domain.ActionDocument;
import core.log.domain.EventDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.kafka.ActionLogMessageHandler;
import core.log.kafka.EventMessageHandler;
import core.log.kafka.StatMessageHandler;
import core.log.service.ActionLogForwarder;
import core.log.service.ActionService;
import core.log.service.EventForwarder;
import core.log.service.EventService;
import core.log.service.IndexOption;
import core.log.service.IndexService;
import core.log.service.JobConfig;
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
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");

        configureSearch();

        configureIndexOption();
        IndexService indexService = bind(IndexService.class);
        bind(ActionService.class);
        bind(StatService.class);
        bind(EventService.class);

        onStartup(indexService::createIndexTemplates);

        configureKibanaService();
        Forwarders forwarders = configureLogForwarders();
        configureKafka(forwarders);
        configureJob();

        load(new DiagramModule());
    }

    private void configureKibanaService() {
        // explicitly use all properties in case runtime env doesn't define any, otherwise startup may fail with unused property error
        Optional<String> kibanaURL = property("app.kibana.url");
        String banner = property("app.kibana.banner").orElse("");
        kibanaURL.ifPresent(url -> {
            HTTPClient client = HTTPClient.builder().maxRetries(5).build();  // create ad hoc http client, will be recycled once done
            onStartup(() -> new Thread(new KibanaService(url, banner, client)::importObjects, "kibana").start());
        });
    }

    private void configureIndexOption() {
        var option = new IndexOption();
        option.numberOfShards = Integer.parseInt(property("app.index.shards").orElse("1"));     // with small cluster one shard has the best performance, for larger cluster use kube env to customize
        option.refreshInterval = property("app.index.refresh.interval").orElse("10s");          // use longer refresh to tune load on log es
        bind(option);
    }

    private void configureKafka(Forwarders forwarders) {
        kafka().poolSize(Runtime.getRuntime().availableProcessors() == 1 ? 1 : 2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get at least 1M message
        kafka().maxPoll(2000, 3 * 1024 * 1024);     // get 3M message at max

        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(new ActionLogMessageHandler(forwarders.action)));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(new EventMessageHandler(forwarders.event)));
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

    private void configureJob() {
        var config = new JobConfig();
        // delete index older than retention days, override by env APP_INDEX_RETENTION_DAYS if needed
        config.indexRetentionDays = Integer.parseInt(property("app.index.retention.days").orElse("30"));
        // close index older than open days, override by env APP_INDEX_OPEN_DAYS if needed
        config.indexOpenDays = Integer.parseInt(property("app.index.open.days").orElse("7"));
        bind(config);

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }

    private Forwarders configureLogForwarders() {
        var forwarders = new Forwarders();
        // configure by env APP_LOG_FORWARD_CONFIG
        String configValue = property("app.log.forward.config").orElse(null);
        if (configValue != null) {
            Bean.register(LogForwardConfig.class);
            LogForwardConfig config = Bean.fromJSON(LogForwardConfig.class, configValue);
            kafka("forward").uri(config.kafkaURI);
            LogForwardConfig.Forward actionConfig = config.action;
            if (actionConfig != null) {
                MessagePublisher<ActionLogMessage> publisher = kafka("forward").publish(ActionLogMessage.class);
                forwarders.action = new ActionLogForwarder(publisher, actionConfig.topic, actionConfig.apps, actionConfig.ignoreErrorCodes);
            }
            LogForwardConfig.Forward eventConfig = config.event;
            if (eventConfig != null) {
                MessagePublisher<EventMessage> publisher = kafka("forward").publish(EventMessage.class);
                forwarders.event = new EventForwarder(publisher, eventConfig.topic, eventConfig.apps, eventConfig.ignoreErrorCodes);
            }
        }
        return forwarders;
    }

    static class Forwarders {
        ActionLogForwarder action;
        EventForwarder event;
    }
}
