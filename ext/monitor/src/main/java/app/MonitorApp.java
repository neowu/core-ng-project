package app;

import app.monitor.AlertConfig;
import app.monitor.MonitorConfig;
import app.monitor.alert.AlertService;
import app.monitor.job.ElasticSearchCollector;
import app.monitor.job.MonitorJob;
import app.monitor.job.RedisCollector;
import app.monitor.kafka.ActionLogMessageHandler;
import app.monitor.kafka.EventMessageHandler;
import app.monitor.kafka.StatMessageHandler;
import app.monitor.slack.SlackClient;
import app.monitor.slack.SlackMessageAPIRequest;
import app.monitor.slack.SlackMessageAPIResponse;
import core.framework.http.HTTPClient;
import core.framework.json.Bean;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.framework.redis.Redis;

import java.time.Duration;
import java.util.Map;

/**
 * @author ericchung
 */
public class MonitorApp extends App {
    public static final String MONITOR_APP = "monitor";

    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");

        property("app.slack.token").ifPresent(this::configureSlackClient);
        property("app.alert.config").ifPresent(this::configureAlert);
        property("app.monitor.config").ifPresent(this::configureMonitor);
    }

    private void configureAlert(String alertConfig) {
        Bean.register(AlertConfig.class);
        AlertConfig config = Bean.fromJSON(AlertConfig.class, alertConfig);
        bind(new AlertService(config));
        kafka().poolSize(Runtime.getRuntime().availableProcessors() == 1 ? 1 : 2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get 1M message
        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(EventMessageHandler.class));
    }

    private void configureMonitor(String monitorConfig) {
        Bean.register(MonitorConfig.class);
        MonitorConfig config = Bean.fromJSON(MonitorConfig.class, monitorConfig);
        MessagePublisher<StatMessage> publisher = kafka().publish(LogTopics.TOPIC_STAT, StatMessage.class);
        if (!config.redis.isEmpty()) {
            configureRedisJob(publisher, config.redis);
        }
        if (!config.es.isEmpty()) {
            configureESJob(publisher, config.es);
        }
    }

    private void configureESJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.ElasticSearchConfig> config) {
        HTTPClient httpClient = HTTPClient.builder().build();
        for (Map.Entry<String, MonitorConfig.ElasticSearchConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.ElasticSearchConfig esConfig = entry.getValue();
            for (String host : esConfig.hosts) {
                var collector = new ElasticSearchCollector(httpClient, host);
                collector.highHeapUsageThreshold = esConfig.highHeapUsageThreshold;
                collector.highDiskUsageThreshold = esConfig.highDiskUsageThreshold;
                schedule().fixedRate("es-" + host, new MonitorJob(collector, app, host, publisher), Duration.ofSeconds(10));
            }
        }
    }

    private void configureRedisJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.RedisConfig> config) {
        for (Map.Entry<String, MonitorConfig.RedisConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.RedisConfig redisConfig = entry.getValue();
            for (String host : redisConfig.hosts) {
                redis(host).host(host);
                redis(host).poolSize(1, 1);
                Redis redis = redis(host).client();
                var collector = new RedisCollector(redis);
                collector.highMemUsageThreshold = redisConfig.highMemUsageThreshold;
                schedule().fixedRate("redis-" + host, new MonitorJob(collector, app, host, publisher), Duration.ofSeconds(10));
            }
        }
    }

    private void configureSlackClient(String slackToken) {
        HTTPClient httpClient = HTTPClient.builder()
                                          .maxRetries(3)
                                          .retryWaitTime(Duration.ofSeconds(2))   // slack has rate limit with 1 message per second, here to slow down further when hit limit, refer to https://api.slack.com/docs/rate-limits
                                          .build();

        Bean.register(SlackMessageAPIRequest.class);
        Bean.register(SlackMessageAPIResponse.class);
        bind(new SlackClient(httpClient, slackToken));
    }
}
