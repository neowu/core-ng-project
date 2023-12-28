package core;

import core.framework.http.HTTPClient;
import core.framework.json.Bean;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.App;
import core.framework.module.KafkaConfig;
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
import core.log.service.EventForwarder;
import core.log.service.IndexOption;
import core.log.service.IndexService;
import core.log.service.JobConfig;
import core.log.service.KibanaService;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        // not using SystemModule, and not put sys.log.appender property, to prevent log processor from sending its own action log to same log-kafka it's pulling from
        loadProperties("sys.properties");
        loadProperties("app.properties");

        configureSearch();

        configureIndexOption();
        IndexService indexService = bind(IndexService.class);
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
        String apiKey = property("app.kibana.apiKey").orElse(null);
        String banner = property("app.kibana.banner").orElse("");
        kibanaURL.ifPresent(url -> {
            HTTPClient client = HTTPClient.builder().maxRetries(5).build();  // create ad hoc http client, will be recycled once done
            onStartup(() -> new Thread(new KibanaService(url, apiKey, banner, client)::importObjects, "kibana").start());
        });
    }

    private void configureIndexOption() {
        var option = new IndexOption();
        option.numberOfShards = Integer.parseInt(property("app.index.shards").orElse("1"));     // with small cluster one shard has the best performance, for larger cluster use kube env to customize
        option.refreshInterval = property("app.index.refresh.interval").orElse("10s");          // use longer refresh to tune load on log es
        bind(option);
    }

    private void configureKafka(Forwarders forwarders) {
        kafka().uri(requiredProperty("sys.kafka.uri"));
        kafka().concurrency(2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get at least 1M message
        kafka().maxPoll(2000, 3 * 1024 * 1024);     // get 3M message at max

        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(new ActionLogMessageHandler(forwarders.action)));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(new EventMessageHandler(forwarders.event)));
    }

    private void configureSearch() {
        SearchConfig search = config(SearchConfig.class);
        search.host(requiredProperty("sys.elasticsearch.host"));
        String apiKeyId = property("sys.elasticsearch.apiKeyId").orElse(null);
        String apiKeySecret = property("sys.elasticsearch.apiKeySecret").orElse(null);
        if (apiKeyId != null && apiKeySecret != null) {
            search.auth(apiKeyId, apiKeySecret);
        }
        search.timeout(Duration.ofSeconds(20)); // use longer timeout/slowES threshold as log indexing can be slower with large batches
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
            KafkaConfig kafka = kafka("forward");
            kafka.uri(config.kafkaURI);
            if (config.action != null) {
                MessagePublisher<ActionLogMessage> publisher = kafka.publish(config.action.topic, ActionLogMessage.class);
                forwarders.action = new ActionLogForwarder(publisher, config.action);
            }
            if (config.event != null) {
                MessagePublisher<EventMessage> publisher = kafka.publish(config.event.topic, EventMessage.class);
                forwarders.event = new EventForwarder(publisher, config.event);
            }
        }
        return forwarders;
    }

    static class Forwarders {
        ActionLogForwarder action;
        EventForwarder event;
    }
}
